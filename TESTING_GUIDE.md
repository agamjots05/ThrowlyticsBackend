# Testing Guide for Throwlytics Backend

This guide will help you test all the video upload and processing endpoints.

## Prerequisites

### 1. PostgreSQL Database
- Ensure PostgreSQL is running
- Database `throwlytics` exists
- User `postgres` with password `password` (or update `application.properties`)

### 2. FFmpeg (for thumbnail generation)
```bash
# macOS
brew install ffmpeg

# Linux
sudo apt-get install ffmpeg

# Verify installation
ffmpeg -version
```

### 3. Python Service
- Navigate to Python service directory
- Install dependencies: `pip install -r requirements.txt`
- Ensure `best.pt` model file exists in the directory
- Start the service (see below)

## Starting the Services

### Step 1: Start Python FastAPI Service

```bash
cd /Users/agamjotsingh/dev/ThrowlyticsYoloModel

# Install dependencies (if not already done)
pip install -r requirements.txt

# Start the service
uvicorn video_processing_service:app --reload --port 8000
```

**Expected output:**
```
✓ Discus model loaded
✓ Pose model loaded
INFO:     Uvicorn running on http://127.0.0.1:8000
```

**Test Python service health:**
```bash
curl http://localhost:8000/health
```

**Expected response:**
```json
{
  "status": "healthy",
  "models_loaded": true
}
```

### Step 2: Start Spring Boot Backend

```bash
cd /Users/agamjotsingh/Downloads/ThrowlyticsBackend

# Using Maven
mvn spring-boot:run

# Or using your IDE:
# Run ThrowlyticsBackendApplication.java
```

**Expected output:**
```
✓ Upload directories initialized
Started ThrowlyticsBackendApplication in X.XXX seconds
```

**Test backend health:**
```bash
curl http://localhost:8080/api/video/health
```

**Expected response:**
```
Video service is running
```

## Testing with Postman

### Step 1: Create a User Account

**Endpoint:** `POST http://localhost:8080/api/auth/signup`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "Test User",
  "email": "test@example.com",
  "password": "password123"
}
```

**Expected Response (201 Created):**
```json
{
  "userId": 1,
  "name": "Test User",
  "email": "test@example.com",
  "planType": "FREE",
  "monthlyTokenLimit": 5,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Note:** The signup endpoint also returns a JWT token, so users can be immediately authenticated after signup.

### Step 2: Login to Get JWT Token

**Endpoint:** `POST http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "userId": 1,
  "name": "Test User",
  "email": "test@example.com",
  "planType": "FREE",
  "monthlyTokenLimit": 5,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**⚠️ IMPORTANT:** Copy the `token` value - you'll need it for authenticated requests!

### Step 3: Upload a Video

**Endpoint:** `POST http://localhost:8080/api/video/upload`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
Content-Type: multipart/form-data
```

**Body (form-data):**
- Key: `file`
- Type: File
- Value: Select a video file (MP4, MOV, AVI, etc.)

**Expected Response (200 OK):**
```json
{
  "throwId": 1,
  "userId": 1,
  "releaseFrame": 42,
  "releaseConfirmed": true,
  "totalFrames": 1361,
  "videoWidth": 1080,
  "videoHeight": 1350,
  "fps": 60,
  "videoUrl": "videos/1/uuid.mp4",
  "thumbnailUrl": "thumbnails/1/uuid.jpg",
  "uploadDate": "2025-01-07T10:30:00",
  "message": "Video processed successfully"
}
```

**Note:** Processing may take 30-60 seconds depending on video length.

### Step 4: Get Throw History

**Endpoint:** `GET http://localhost:8080/api/video/history`

**Headers:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Expected Response (200 OK):**
```json
[
  {
    "throwId": 1,
    "releaseFrame": 42,
    "releaseConfirmed": true,
    "totalFrames": 1361,
    "videoWidth": 1080,
    "videoHeight": 1350,
    "fps": 60,
    "videoUrl": "videos/1/uuid.mp4",
    "thumbnailUrl": "thumbnails/1/uuid.jpg",
    "uploadDate": "2025-01-07T10:30:00"
  }
]
```

## Testing with cURL

### 1. Signup
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 2. Login (Save token to variable)
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }' | jq -r '.token')

echo "Token: $TOKEN"
```

### 3. Upload Video
```bash
curl -X POST http://localhost:8080/api/video/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your/video.mp4"
```

### 4. Get Throw History
```bash
curl -X GET http://localhost:8080/api/video/history \
  -H "Authorization: Bearer $TOKEN"
```

## Testing Error Cases

### 1. Invalid File Type
**Endpoint:** `POST /api/video/upload`

**Body:** Upload a non-video file (e.g., .txt, .pdf)

**Expected Response (400 Bad Request):**
```json
{
  "message": "File must be a video",
  "timestamp": "2025-01-07T10:30:00",
  "details": null
}
```

### 2. File Too Large
**Endpoint:** `POST /api/video/upload`

**Body:** Upload a file larger than 500MB

**Expected Response (400 Bad Request):**
```json
{
  "message": "File size exceeds maximum allowed size of 500MB",
  "timestamp": "2025-01-07T10:30:00",
  "details": null
}
```

### 3. Missing Authentication
**Endpoint:** `POST /api/video/upload`

**Headers:** No Authorization header

**Expected Response (401 Unauthorized):**
```json
{
  "message": "Unauthorized",
  "timestamp": "2025-01-07T10:30:00",
  "details": null
}
```

### 4. Invalid Token
**Endpoint:** `POST /api/video/upload`

**Headers:** `Authorization: Bearer invalid_token`

**Expected Response (401 Unauthorized)**

### 5. Python Service Down
If Python service is not running:

**Expected Response (200 OK, but with error message):**
```json
{
  "throwId": 1,
  "userId": 1,
  "videoUrl": "videos/1/uuid.mp4",
  "message": "Video uploaded successfully, but processing failed. Video processing service unavailable: ..."
}
```

**Note:** Video is still stored even if processing fails.

## Troubleshooting

### Issue: "Connection refused" when calling Python service
**Solution:**
- Ensure Python service is running on port 8000
- Check: `curl http://localhost:8000/health`
- Verify `python.service.url` in `application.properties`

### Issue: "FFmpeg failed with exit code: X"
**Solution:**
- Verify FFmpeg is installed: `ffmpeg -version`
- Check `ffmpeg.path` in `application.properties`
- Ensure FFmpeg is in PATH or provide full path

### Issue: "User not found" error
**Solution:**
- Ensure you're using a valid JWT token
- Token may have expired - login again to get a new token
- Verify user exists in database

### Issue: "Models not loaded" from Python service
**Solution:**
- Ensure `best.pt` file exists in Python service directory
- Check Python service logs for model loading errors
- Verify YOLO model file path

### Issue: Database connection errors
**Solution:**
- Ensure PostgreSQL is running: `pg_isready`
- Verify database credentials in `application.properties`
- Check database exists: `psql -U postgres -l`

### Issue: File storage errors
**Solution:**
- Check write permissions in `uploads/` directory
- Verify disk space is available
- Check `app.upload.videos` and `app.upload.thumbnails` paths

## Quick Test Checklist

- [ ] PostgreSQL is running
- [ ] Python service is running on port 8000
- [ ] Python service health check returns `{"status": "healthy"}`
- [ ] Spring Boot backend is running on port 8080
- [ ] Backend health check returns "Video service is running"
- [ ] Can create user account (signup)
- [ ] Can login and get JWT token
- [ ] Can upload video with valid token
- [ ] Video processing completes successfully
- [ ] Thumbnail is generated
- [ ] Throw history is saved to database
- [ ] Can retrieve throw history

## Expected File Structure After Upload

```
ThrowlyticsBackend/
├── uploads/
│   ├── videos/
│   │   └── 1/
│   │       └── <uuid>.mp4
│   └── thumbnails/
│       └── 1/
│           └── <uuid>.jpg
```

## Database Verification

Check database to verify data was saved:

```sql
-- Connect to database
psql -U postgres -d throwlytics

-- View throw history
SELECT * FROM "throwHistory" ORDER BY "uploadDate" DESC;

-- View users
SELECT * FROM users;
```

## Performance Notes

- Video processing typically takes 30-60 seconds for a 10-30 second video
- Processing time depends on:
  - Video length
  - Video resolution
  - Frame skip setting (default: 3)
- Thumbnail generation is usually fast (< 1 second)

## Next Steps

Once testing is complete, you can:
1. Integrate with the frontend
2. Add more video analysis features
3. Implement video playback endpoints
4. Add user token/plan management
5. Set up production deployment

