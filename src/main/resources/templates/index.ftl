<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <link rel="icon" type="image/svg+xml" href="/static/favicon.svg">

    <!-- HTMX -->
    <script src="https://unpkg.com/htmx.org@2.0.3"></script>

    <!-- Tailwind CSS + DaisyUI -->
    <link href="https://cdn.jsdelivr.net/npm/daisyui@4.12.14/dist/full.min.css" rel="stylesheet" type="text/css" />
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 p-4">
    <div class="container mx-auto max-w-6xl">
        <!-- Header Card -->
        <div class="card bg-base-100 shadow-2xl mb-6">
            <div class="card-body">
                <h1 class="card-title text-4xl">
                    <img src="/static/favicon.svg" alt="Bob Logo" class="w-12 h-12">
                    Muck
                </h1>
                <p class="text-lg opacity-70">Bob CI/CD Pipeline Monitor</p>
                <div class="badge badge-outline badge-lg mt-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                    Connected to: ${bobUrl}
                </div>
            </div>
        </div>

        <!-- Pipelines Card -->
        <div class="card bg-base-100 shadow-2xl">
            <div class="card-body">
                <h2 class="card-title text-3xl mb-4">
                    Pipelines
                    <span class="loading loading-spinner loading-sm htmx-indicator" id="loading-indicator"></span>
                </h2>

                <div id="htmx-container">
                    <#include "pipelines.ftl">
                </div>
            </div>
        </div>
    </div>
</body>
</html>
