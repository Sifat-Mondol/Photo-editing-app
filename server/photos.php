<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

require_once 'config.php';

// Check if request method is GET
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'GET method required']);
    exit;
}

// Get authorization token
$headers = getallheaders();
$token = isset($headers['Authorization']) ? $headers['Authorization'] : '';

if (empty($token)) {
    echo json_encode(['success' => false, 'message' => 'Authorization token required']);
    exit;
}

// Verify token and get user_id
$stmt = $conn->prepare("SELECT id FROM users WHERE token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid token']);
    exit;
}

$user = $result->fetch_assoc();
$user_id = $user['id'];

// Get all photos for this user
$stmt = $conn->prepare("SELECT id, image_url, description, created_at FROM photos WHERE user_id = ? ORDER BY created_at DESC");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$photos = array();
while ($row = $result->fetch_assoc()) {
    $photos[] = array(
        'id' => $row['id'],
        'imageUrl' => $row['image_url'],
        'description' => $row['description'],
        'createdAt' => $row['created_at']
    );
}

echo json_encode($photos);

$stmt->close();
$conn->close();
?>