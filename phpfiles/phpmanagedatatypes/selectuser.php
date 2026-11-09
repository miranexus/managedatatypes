<?php

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit;
}

$statement = databaseConnection()->prepare(
    'SELECT date, name, email, password FROM textletsretrofit ' .
    'WHERE email = :email AND password = :password'
);
$statement->execute([
    'email' => $_POST['email'] ?? '',
    'password' => $_POST['password'] ?? '',
]);
$user = $statement->fetch();

if (!$user) {
    echo json_encode([
        'success' => false,
        'message' => 'User not found',
    ], JSON_THROW_ON_ERROR);
    exit;
}

echo json_encode([
    'success' => true,
    'message' => 'User logged in successfully',
    ...$user,
], JSON_THROW_ON_ERROR);
