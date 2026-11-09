<?php

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit;
}

$statement = databaseConnection()->prepare(
    'DELETE FROM textletsretrofit WHERE email = :email'
);
$statement->execute(['email' => $_POST['email'] ?? '']);
http_response_code(204);
