<#import "layout.ftl" as layout>

<@layout.page title="Pipelines - Muck">
    <div id="htmx-container"
         hx-get="/pipelines"
         hx-trigger="every 10s"
         hx-target="this"
         hx-select="#htmx-content"
         hx-indicator="#loading-indicator">
        <div id="htmx-content" class="card bg-base-100 shadow-sm">
            <div class="card-body">
                <div class="flex items-center justify-between mb-4">
                    <h2 class="text-xl font-semibold">Pipelines</h2>
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
                                <div class="rounded-lg border border-base-300 bg-base-100 hover:border-base-content/20 transition-colors duration-150 cursor-pointer p-4">
                                    <div class="flex items-center justify-between">
                                        <div class="flex-1">
                                            <h3 class="font-medium">${pipeline.name}</h3>
                                            <div class="text-sm text-base-content/60 mt-1">${pipeline.group}</div>
                                        </div>
                                        <div class="flex items-center gap-2">
                                            <#if pipeline.status == "running">
                                                <span class="badge badge-outline badge-sm gap-1 text-info border-info">
                                                    <span class="loading loading-spinner loading-xs"></span>
                                                    ${pipeline.status}
                                                </span>
                                            <#elseif pipeline.status == "passed">
                                                <span class="badge badge-outline badge-sm text-success border-success">${pipeline.status}</span>
                                            <#elseif pipeline.status == "failed">
                                                <span class="badge badge-outline badge-sm text-error border-error">${pipeline.status}</span>
                                            <#else>
                                                <span class="badge badge-outline badge-sm">${pipeline.status}</span>
                                            </#if>
                                            <button class="btn btn-ghost btn-xs btn-circle"
                                                    hx-post="/start?group=${pipeline.group}&name=${pipeline.name}"
                                                    hx-swap="none"
                                                    onclick="event.stopPropagation()">
                                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                    <circle cx="12" cy="12" r="10"/>
                                                    <path d="M10 8l6 4-6 4V8z" fill="currentColor" stroke="none"/>
                                                </svg>
                                            </button>
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
