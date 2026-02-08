<#macro page title>
<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <link rel="icon" type="image/svg+xml" href="/static/favicon.svg">

    <!-- Inter Font -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- HTMX -->
    <script src="/static/js/htmx.min.js"></script>

    <!-- Tailwind CSS + DaisyUI -->
    <link href="/static/css/daisyui.min.css" rel="stylesheet" type="text/css" />
    <script src="/static/js/tailwind.min.js"></script>

    <!-- Custom styles -->
    <link href="/static/style.css" rel="stylesheet" type="text/css" />
</head>
<body class="min-h-screen bg-base-200">
    <div class="container mx-auto max-w-5xl px-4 py-6">
        <!-- Header -->
        <div class="flex items-center gap-3 mb-6">
            <a href="/" class="flex items-center gap-3 hover:opacity-80 transition-opacity">
                <img src="/static/favicon.svg" alt="Bob" class="w-8 h-8">
                <span class="font-semibold text-lg">Bob the Builder</span>
            </a>
            <span class="loading loading-spinner loading-sm htmx-indicator ml-auto" id="loading-indicator"></span>
        </div>

        <!-- Page Content -->
        <#nested>
    </div>
</body>
</html>
</#macro>
