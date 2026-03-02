<#import "layout.ftl" as layout>

<@layout.page title="Runs - Muck" activePage="pipelines" connected=connected bobUrl=bobUrl>
    <div id="htmx-container">
        <div id="htmx-content"
             hx-get=""
             hx-trigger="every 10s"
             hx-target="#htmx-container"
             hx-select="#htmx-content"
             hx-swap="innerHTML"
             hx-indicator="#loading-indicator">
            <div class="flex items-center justify-between p-4 border-b border-base-300">
                <div class="flex items-center gap-3">
                    <a href="/pipelines" hx-boost="true" class="btn btn-ghost btn-sm btn-circle">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                        </svg>
                    </a>
                    <div>
                        <h2 class="font-semibold text-lg">${name}</h2>
                        <div class="text-sm text-base-content/60">${group}</div>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <button class="btn btn-outline btn-sm gap-1"
                            hx-post="/start?group=${group}&name=${name}"
                            hx-swap="none">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"/>
                            <path d="M10 8l6 4-6 4V8z" fill="currentColor" stroke="none"/>
                        </svg>
                        Start
                    </button>
                    <a class="btn btn-outline btn-sm gap-1"
                       href="/pipeline/yaml?group=${group}&name=${name}"
                       download="${group}-${name}.yaml">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                        </svg>
                        Download
                    </a>
                    <button class="btn btn-outline btn-sm gap-1 text-error border-error hover:bg-error hover:text-error-content"
                            hx-delete="/delete?group=${group}&name=${name}"
                            hx-confirm="Delete pipeline ${group} ${name}?">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                        Delete
                    </button>
                </div>
            </div>

            <div class="p-4">
                <#if runs?has_content>
                    <div class="overflow-x-auto">
                        <table class="table table-lg">
                            <thead>
                                <tr>
                                    <th>Run ID</th>
                                    <th>Status</th>
                                    <th>Scheduled</th>
                                    <th>Completed</th>
                                    <th class="w-28">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list runs as run>
                                    <tr class="hover">
                                        <td class="font-mono text-sm">${run.runId}</td>
                                        <td>
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
                                        </td>
                                        <td class="text-sm text-base-content/60">
                                            <#if run.scheduledAt?has_content>
                                                ${run.scheduledAt?datetime.iso?string("MMM d, yyyy HH:mm")}
                                            </#if>
                                        </td>
                                        <td class="text-sm text-base-content/60">
                                            <#if run.completedAt?has_content>
                                                ${run.completedAt?datetime.iso?string("MMM d, yyyy HH:mm")}
                                            </#if>
                                        </td>
                                        <td>
                                            <div class="flex items-center gap-1">
                                                <#if run.status == "running">
                                                    <button class="btn btn-ghost btn-xs" title="Stop run"
                                                            hx-post="/run/stop?run=${run.runId}"
                                                            hx-swap="none">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                                            <rect x="6" y="6" width="12" height="12" rx="1" fill="currentColor" stroke="none"/>
                                                        </svg>
                                                    </button>
                                                    <button class="btn btn-ghost btn-xs" title="Pause pipeline"
                                                            hx-post="/run/pause?group=${group}&name=${name}"
                                                            hx-swap="none">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                                            <rect x="6" y="5" width="4" height="14" rx="1" fill="currentColor" stroke="none"/>
                                                            <rect x="14" y="5" width="4" height="14" rx="1" fill="currentColor" stroke="none"/>
                                                        </svg>
                                                    </button>
                                                <#elseif run.status == "paused">
                                                    <button class="btn btn-ghost btn-xs" title="Unpause pipeline"
                                                            hx-post="/run/unpause?group=${group}&name=${name}"
                                                            hx-swap="none">
                                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                            <circle cx="12" cy="12" r="10"/>
                                                            <path d="M10 8l6 4-6 4V8z" fill="currentColor" stroke="none"/>
                                                        </svg>
                                                    </button>
                                                </#if>
                                                <#if run.logger?has_content>
                                                    <a class="btn btn-ghost btn-xs"
                                                       href="/logs?group=${group}&name=${name}&run=${run.runId}">
                                                        Logs
                                                    </a>
                                                </#if>
                                            </div>
                                        </td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
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
