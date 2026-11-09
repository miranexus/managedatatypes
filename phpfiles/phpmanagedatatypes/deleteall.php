<?php

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    exit;
}

databaseConnection()->exec('DELETE FROM textletsretrofit');
http_response_code(204);
