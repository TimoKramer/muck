<#macro page title activePage="" connected=false bobUrl="">
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
<body class="min-h-screen bg-base-100">
    <div class="flex min-h-screen">
        <!-- Sidebar -->
        <nav class="w-64 border-r border-base-300 flex-shrink-0 flex flex-col">
            <div class="p-4 border-b border-base-300">
                <a href="/" class="flex items-center gap-3 hover:opacity-80 transition-opacity">
                    <img src="/static/favicon.svg" alt="Bob" class="w-8 h-8">
                    <span class="font-semibold text-xl">Bob the Builder</span>
                </a>
            </div>
            <ul class="menu menu-md p-4 gap-1 flex-1">
                <li>
                    <a href="/pipelines" class="<#if activePage == 'pipelines'>active</#if>">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                        </svg>
                        Pipelines
                    </a>
                </li>
                <li>
                    <a href="/loggers" class="<#if activePage == 'loggers'>active</#if>">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        Loggers
                    </a>
                </li>
                <li>
                    <a href="/resource-providers" class="<#if activePage == 'resource-providers'>active</#if>">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" />
                        </svg>
                        Resource Providers
                    </a>
                </li>
                <li>
                    <a href="/artifact-stores" class="<#if activePage == 'artifact-stores'>active</#if>">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                        </svg>
                        Artifact Stores
                    </a>
                </li>
            </ul>
            <div class="p-4 border-t border-base-300">
                <#if connected>
                    <div class="badge badge-outline badge-sm gap-1 text-success border-success" title="${bobUrl}">
                        <span class="w-1.5 h-1.5 rounded-full bg-success animate-pulse"></span>
                        Connected
                    </div>
                <#else>
                    <div class="badge badge-outline badge-sm gap-1 text-error border-error">
                        <span class="w-1.5 h-1.5 rounded-full bg-error"></span>
                        Disconnected
                    </div>
                </#if>
            </div>
        </nav>

        <!-- Main content -->
        <main class="flex-1 min-w-0">
            <#nested>
        </main>
    </div>
</body>
</html>
</#macro>
