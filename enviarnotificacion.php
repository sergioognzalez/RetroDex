<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');

$serviceAccountPath = __DIR__ . DIRECTORY_SEPARATOR . 'service-account.json';
$fcmScope = 'https://www.googleapis.com/auth/firebase.messaging';
$tokenUri = 'https://oauth2.googleapis.com/token';

function respond(int $statusCode, array $payload): void
{
    http_response_code($statusCode);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    exit;
}

function base64UrlEncode(string $data): string
{
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function readPostField(string $primary, string $fallback = ''): string
{
    $value = $_POST[$primary] ?? $_POST[$fallback] ?? '';
    return is_string($value) ? trim($value) : '';
}

function buildJwt(string $clientEmail, string $privateKey, string $tokenUri, string $scope): string
{
    $header = ['alg' => 'RS256', 'typ' => 'JWT'];
    $now = time();
    $claims = [
        'iss' => $clientEmail,
        'scope' => $scope,
        'aud' => $tokenUri,
        'iat' => $now,
        'exp' => $now + 3600,
    ];

    $unsignedToken = base64UrlEncode(json_encode($header, JSON_UNESCAPED_SLASHES))
        . '.'
        . base64UrlEncode(json_encode($claims, JSON_UNESCAPED_SLASHES));

    $signature = '';
    $success = openssl_sign($unsignedToken, $signature, $privateKey, OPENSSL_ALGO_SHA256);
    if (!$success) {
        respond(500, [
            'success' => false,
            'error' => 'No se pudo firmar el JWT con la clave privada.',
        ]);
    }

    return $unsignedToken . '.' . base64UrlEncode($signature);
}

function httpPost(string $url, array $headers, string $body): array
{
    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_POSTFIELDS => $body,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 20,
    ]);

    $responseBody = curl_exec($ch);
    $curlError = curl_error($ch);
    $httpCode = (int) curl_getinfo($ch, CURLINFO_RESPONSE_CODE);
    curl_close($ch);

    if ($responseBody === false) {
        return [
            'ok' => false,
            'http_code' => $httpCode,
            'error' => 'Error de cURL: ' . $curlError,
            'raw_body' => null,
            'json' => null,
        ];
    }

    $decoded = json_decode($responseBody, true);

    return [
        'ok' => $httpCode >= 200 && $httpCode < 300,
        'http_code' => $httpCode,
        'error' => null,
        'raw_body' => $responseBody,
        'json' => is_array($decoded) ? $decoded : null,
    ];
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(405, [
        'success' => false,
        'error' => 'Método no permitido. Usa POST.',
    ]);
}

if (!file_exists($serviceAccountPath)) {
    respond(500, [
        'success' => false,
        'error' => 'No se encontró service-account.json en la misma carpeta del PHP.',
        'expected_path' => $serviceAccountPath,
    ]);
}

$serviceAccountRaw = file_get_contents($serviceAccountPath);
if ($serviceAccountRaw === false) {
    respond(500, [
        'success' => false,
        'error' => 'No se pudo leer service-account.json.',
    ]);
}

$serviceAccount = json_decode($serviceAccountRaw, true);
if (!is_array($serviceAccount)) {
    respond(500, [
        'success' => false,
        'error' => 'service-account.json no contiene un JSON válido.',
    ]);
}

$projectId = $serviceAccount['project_id'] ?? '';
$clientEmail = $serviceAccount['client_email'] ?? '';
$privateKey = $serviceAccount['private_key'] ?? '';

if ($projectId === '' || $clientEmail === '' || $privateKey === '') {
    respond(500, [
        'success' => false,
        'error' => 'Faltan campos requeridos en service-account.json.',
        'required_fields' => ['project_id', 'client_email', 'private_key'],
    ]);
}

$title = readPostField('title', 'nombre');
$body = readPostField('body', 'mensaje');

if ($title === '' || $body === '') {
    respond(400, [
        'success' => false,
        'error' => 'Faltan datos POST. Debes enviar title/body o nombre/mensaje.',
    ]);
}

$jwt = buildJwt($clientEmail, $privateKey, $tokenUri, $fcmScope);

$tokenResponse = httpPost(
    $tokenUri,
    ['Content-Type: application/x-www-form-urlencoded'],
    http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt,
    ], '', '&', PHP_QUERY_RFC3986)
);

if (!$tokenResponse['ok'] || !isset($tokenResponse['json']['access_token'])) {
    respond(500, [
        'success' => false,
        'error' => 'No se pudo obtener el access token de Google.',
        'details' => [
            'http_code' => $tokenResponse['http_code'],
            'curl_error' => $tokenResponse['error'],
            'google_response' => $tokenResponse['json'] ?? $tokenResponse['raw_body'],
        ],
    ]);
}

$accessToken = $tokenResponse['json']['access_token'];
$fcmUrl = 'https://fcm.googleapis.com/v1/projects/' . rawurlencode($projectId) . '/messages:send';

$messagePayload = [
    'message' => [
        'topic' => 'allUsers',
        'notification' => [
            'title' => $title,
            'body' => $body,
        ],
        'android' => [
            'priority' => 'high',
            'notification' => [
                'icon' => 'ic_noti',
            ],
        ],
    ],
];

$fcmResponse = httpPost(
    $fcmUrl,
    [
        'Authorization: Bearer ' . $accessToken,
        'Content-Type: application/json; charset=utf-8',
    ],
    json_encode($messagePayload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES)
);

if (!$fcmResponse['ok']) {
    respond(500, [
        'success' => false,
        'error' => 'Firebase rechazó el envío de la notificación.',
        'details' => [
            'http_code' => $fcmResponse['http_code'],
            'firebase_response' => $fcmResponse['json'] ?? $fcmResponse['raw_body'],
        ],
    ]);
}

respond(200, [
    'success' => true,
    'message' => 'Notificación enviada correctamente al topic allUsers.',
    'project_id' => $projectId,
    'topic' => 'allUsers',
    'notification' => [
        'title' => $title,
        'body' => $body,
    ],
    'firebase_response' => $fcmResponse['json'] ?? $fcmResponse['raw_body'],
]);
