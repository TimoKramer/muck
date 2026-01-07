<#macro page title>
<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <link rel="icon" type="image/svg+xml" href="/static/favicon.svg">

    <script src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.8/dist/htmx.min.js" integrity="sha384-/TgkGk7p307TH7EXJDuUlgG3Ce1UVolAOFopFekQkkXihi5u/6OCvVKyz1W+idaz" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-sse@2.2.4" integrity="sha384-A986SAtodyH8eg8x8irJnYUk7i9inVQqYigD6qZ9evobksGNIXfeFvDwLSHcp31N" crossorigin="anonymous"></script>

    <!-- Tailwind CSS + DaisyUI -->
    <link href="https://cdn.jsdelivr.net/npm/daisyui@4.12.14/dist/full.min.css" rel="stylesheet" type="text/css" />
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="min-h-screen bg-base-200">
    <div class="container mx-auto max-w-5xl px-4 py-6">
        <!-- Header -->
        <div class="flex items-center gap-3 mb-6">
            <img src="/static/favicon.svg" alt="Bob" class="w-8 h-8">
            <span class="font-semibold text-lg">Muck</span>
            <span class="text-base-content/50 text-sm">Bob CD Monitor</span>
            <span class="loading loading-spinner loading-sm htmx-indicator ml-auto" id="loading-indicator"></span>
        </div>

        <!-- Page Content -->
        <#nested>
    </div>
</body>
</html>
</#macro>
