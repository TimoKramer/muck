<#import "layout.ftl" as layout>

<@layout.page title="Muck - Bob CI/CD Monitor" bobUrl=bobUrl>
    <span class="loading loading-spinner loading-sm htmx-indicator" id="loading-indicator"></span>

    <div id="htmx-container"
         hx-get="/pipelines"
         hx-trigger="load, every 10s"
         hx-target="this"
         hx-select="#htmx-content"
         hx-indicator="#loading-indicator">
        <div class="flex flex-col items-center justify-center py-16">
            <span class="loading loading-dots loading-lg"></span>
            <p class="mt-4 text-lg opacity-60">Loading pipelines...</p>
        </div>
    </div>
</@layout.page>
