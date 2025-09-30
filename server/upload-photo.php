<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// Log file for debugging
$log_file = 'upload_log.txt';
file_put_contents($log_file, "\n\n=== NEW UPLOAD REQUEST ===\n", FILE_APPEND);
file_put_contents($log_file, date('Y-m-d H:i:s') . "\n", FILE_APPEND);

require_once 'config.php';

// Check request method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    file_put_contents($log_file, "ERROR: Not POST method\n", FILE_APPEND);
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'POST method required']);
    exit;
}

// Get authorization token
$headers = getallheaders();
$token = '';

if (isset($headers['Authorization'])) {
    $token = $headers['Authorization'];
} elseif (isset($headers['authorization'])) {
    $token = $headers['authorization'];
}

file_put_contents($log_file, "Token: " . $token . "\n", FILE_APPEND);

if (empty($token)) {
    file_put_contents($log_file, "ERROR: No token\n", FILE_APPEND);
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Authorization token required']);
    exit;
}

// Verify token and get user_id
$stmt = $conn->prepare("SELECT id, username FROM users WHERE token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    file_put_contents($log_file, "ERROR: Invalid token\n", FILE_APPEND);
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Invalid token']);
    exit;
}

$user = $result->fetch_assoc();
$user_id = $user['id'];
file_put_contents($log_file, "User ID: " . $user_id . "\n", FILE_APPEND);

// Get POST data
$image = isset($_POST['image']) ? $_POST['image'] : '';
$description = isset($_POST['description']) ? $_POST['description'] : 'No description';

file_put_contents($log_file, "Description: " . $description . "\n", FILE_APPEND);
file_put_contents($log_file, "Image data length: " . strlen($image) . "\n", FILE_APPEND);

if (empty($image)) {
    file_put_contents($log_file, "ERROR: No image data\n", FILE_APPEND);
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Image data required']);
    exit;
}

// Create uploads directory if it doesn't exist
$upload_dir = 'uploads/';
if (!file_exists($upload_dir)) {
    mkdir($upload_dir, 0777, true);
    file_put_contents($log_file, "Created uploads directory\n", FILE_APPEND);
}

// Decode base64 image
$image_data = base64_decode($image);
if ($image_data === false) {
    file_put_contents($log_file, "ERROR: Failed to decode base64\n", FILE_APPEND);
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid image data']);
    exit;
}

file_put_contents($log_file, "Decoded image size: " . strlen($image_data) . " bytes\n", FILE_APPEND);

// Generate unique filename
$filename = uniqid() . '_' . time() . '.jpg';
$filepath = $upload_dir . $filename;

file_put_contents($log_file, "Filepath: " . $filepath . "\n", FILE_APPEND);

// Save image to file
$bytes_written = file_put_contents($filepath, $image_data);
if ($bytes_written === false) {
    file_put_contents($log_file, "ERROR: Failed to save image file\n", FILE_APPEND);
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Failed to save image']);
    exit;
}

file_put_contents($log_file, "Image saved: " . $bytes_written . " bytes\n", FILE_APPEND);

// Get the full URL for the image
$protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
$host = $_SERVER['HTTP_HOST'];
$image_url = $protocol . "://" . $host . "/BcsBangla/" . $filepath;

file_put_contents($log_file, "Image URL: " . $image_url . "\n", FILE_APPEND);

// Insert into database
$stmt = $conn->prepare("INSERT INTO photos (user_id, image_url, description, created_at) VALUES (?, ?, ?, NOW())");
if (!$stmt) {
    file_put_contents($log_file, "ERROR: Prepare failed: " . $conn->error . "\n", FILE_APPEND);
    unlink($filepath);
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit;
}

$stmt->bind_param("iss", $user_id, $image_url, $description);

if ($stmt->execute()) {
    $photo_id = $stmt->insert_id;
    file_put_contents($log_file, "SUCCESS: Photo inserted with ID: " . $photo_id . "\n", FILE_APPEND);
    
    http_response_code(200);
    echo json_encode([
        'success' => true, 
        'message' => 'Photo uploaded successfully',
        'photo_id' => $photo_id,
        'image_url' => $image_url
    ]);
} else {
    file_put_contents($log_file, "ERROR: Execute failed: " . $stmt->error . "\n", FILE_APPEND);
    unlink($filepath);
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>