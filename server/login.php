<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'POST method required']);
    exit;
}

$email = isset($_POST['email']) ? trim($_POST['email']) : '';
$password = isset($_POST['password']) ? $_POST['password'] : '';

if (empty($email) || empty($password)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Email and password are required']);
    exit;
}

// Get user by email
$stmt = $conn->prepare("SELECT id, username, email, password, token FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Invalid email or password']);
    exit;
}

$user = $result->fetch_assoc();

// Verify password
if (!password_verify($password, $user['password'])) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Invalid email or password']);
    exit;
}

// Generate new token if not exists
$token = $user['token'];
if (empty($token)) {
    $token = bin2hex(random_bytes(32));
    $update_stmt = $conn->prepare("UPDATE users SET token = ? WHERE id = ?");
    $update_stmt->bind_param("si", $token, $user['id']);
    $update_stmt->execute();
    $update_stmt->close();
}

http_response_code(200);
echo json_encode([
    'success' => true,
    'message' => 'Login successful',
    'token' => $token,
    'username' => $user['username']
]);

$stmt->close();
$conn->close();
?>