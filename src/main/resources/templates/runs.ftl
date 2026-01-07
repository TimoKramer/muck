<#import "layout.ftl" as layout>

<@layout.page title="Runs - Muck">
    <div id="htmx-container"
         hx-get=""
         hx-trigger="every 10s"
         hx-target="this"
         hx-select="#htmx-content"
         hx-indicator="#loading-indicator">
        <div id="htmx-content" class="card bg-base-100 shadow-sm">
            <div class="card-body">
                <div class="flex items-center justify-between mb-4">
                    <a href="/pipelines" hx-boost="true" class="btn btn-ghost btn-sm gap-1">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                        </svg>
                        Back
                    </a>
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

                <div class="mb-4">
                    <h2 class="text-xl font-semibold">${name}</h2>
                    <div class="text-sm text-base-content/60 mt-1">${group}</div>
                </div>

                <#if runs?has_content>
                    <div class="space-y-3">
                        <#list runs as run>
                            <div class="rounded-lg border border-base-300 bg-base-100 hover:border-base-content/20 transition-colors duration-150 p-4">
                                <div class="flex items-center justify-between gap-4">
                                    <div class="flex-1 min-w-0">
                                        <div class="flex items-center gap-2 mb-1">
                                            <span class="font-mono text-sm truncate">${run.runId}</span>
                                            <#if run.status == "running">
                                                <span class="badge badge-outline badge-sm gap-1 text-info border-info">
                                                    <span class="loading loading-spinner loading-xs"></span>
                                                    ${run.status}
                                                </span>
                                            <#elseif run.status == "passed">
                                                <span class="badge badge-outline badge-sm text-success border-success">${run.status}</span>
                                            <#elseif run.status == "failed">
                                                <span class="badge badge-outline badge-sm text-error border-error">${run.status}</span>
                                            <#else>
                                                <span class="badge badge-outline badge-sm">${run.status}</span>
                                            </#if>
                                        </div>
                                        <div class="text-xs text-base-content/60 flex flex-wrap gap-x-4 gap-y-1">
                                            <#if run.scheduledAt?has_content>
                                                <span>Scheduled: ${run.scheduledAt}</span>
                                            </#if>
                                            <#if run.completedAt?has_content>
                                                <span>Completed: ${run.completedAt}</span>
                                            </#if>
                                        </div>
                                    </div>
                                    <#if run.logger?has_content>
                                        <a class="btn btn-outline btn-sm"
                                           href="/logs?group=${group}&name=${name}&run=${run.runId}">
                                            Logs
                                        </a>
                                    </#if>
                                </div>
                            </div>
                        </#list>
                    </div>
                <#else>
                    <div class="alert alert-warning">
                        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <div>
                            <h3 class="font-bold">No runs found</h3>
                            <div class="text-sm">This pipeline hasn't been executed yet.</div>
                        </div>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</@layout.page>
