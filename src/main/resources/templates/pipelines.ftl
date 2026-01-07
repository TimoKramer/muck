<#import "layout.ftl" as layout>

<@layout.page title="Pipelines - Muck" bobUrl=bobUrl connected=connected>
    <div id="htmx-container"
         hx-get="/pipelines"
         hx-trigger="every 10s"
         hx-target="this"
         hx-select="#htmx-content"
         hx-indicator="#loading-indicator">
        <div id="htmx-content" class="card bg-base-100 shadow-2xl">
            <div class="card-body">
                <h2 class="card-title text-3xl mb-4">Pipelines</h2>

                <#if pipelines?has_content>
                    <div class="space-y-3">
                        <#list pipelines as pipeline>
                            <a class="block"
                               hx-get="/runs?group=${pipeline.group}&name=${pipeline.name}"
                               hx-target="#htmx-container"
                               hx-select="#htmx-container"
                               hx-swap="outerHTML"
                               hx-indicator="#loading-indicator"
                               hx-push-url="true">
                                <div class="card bg-base-200 hover:bg-base-300 transition-all duration-200 hover:shadow-lg cursor-pointer">
                                    <div class="card-body p-4">
                                        <div class="flex items-center justify-between">
                                            <div class="flex-1">
                                                <h3 class="font-bold text-lg">${pipeline.name}</h3>
                                                <div class="text-sm opacity-60 mt-1">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 inline mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                                                    </svg>
                                                    Group: ${pipeline.group}
                                                </div>
                                            </div>
                                            <div class="flex items-center gap-2">
                                                <#if pipeline.status == "running">
                                                    <span class="badge badge-success badge-lg">
                                                        <span class="loading loading-spinner loading-xs mr-1"></span>
                                                        ${pipeline.status}
                                                    </span>
                                                <#elseif pipeline.status == "passed">
                                                    <span class="badge badge-info badge-lg">${pipeline.status}</span>
                                                <#elseif pipeline.status == "failed">
                                                    <span class="badge badge-error badge-lg">${pipeline.status}</span>
                                                <#else>
                                                    <span class="badge badge-ghost badge-lg">${pipeline.status}</span>
                                                </#if>
                                                <button class="btn btn-ghost btn-sm btn-circle"
                                                        hx-post="/start?group=${pipeline.group}&name=${pipeline.name}"
                                                        hx-swap="none"
                                                        onclick="event.stopPropagation()">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                        <circle cx="12" cy="12" r="10"/>
                                                        <path d="M10 8l6 4-6 4V8z" fill="currentColor" stroke="none"/>
                                                    </svg>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        </#list>
                    </div>
                <#else>
                    <div class="alert alert-warning">
                        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <div>
                            <h3 class="font-bold">No pipelines found</h3>
                            <div class="text-sm">Make sure Bob is running at the configured URL.</div>
                        </div>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</@layout.page>
