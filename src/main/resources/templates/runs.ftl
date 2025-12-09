<#import "layout.ftl" as layout>

<@layout.page title="Runs - Muck" bobUrl=bobUrl>
    <div class="card bg-base-100 shadow-2xl">
        <div class="card-body">
            <div class="mb-4">
                <a href="/" class="btn btn-ghost btn-sm gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to Pipelines
                </a>

                <div class="mt-4 mb-2">
                    <h2 class="text-3xl font-bold">${name}</h2>
                    <div class="text-sm opacity-60 flex items-center gap-2 mt-1">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                        </svg>
                        Group: ${group}
                    </div>
                </div>
            </div>

            <#if runs?has_content>
                <div class="space-y-3">
                    <#list runs as run>
                        <div class="card bg-base-200 hover:bg-base-300 transition-all duration-200 hover:shadow-lg">
                            <div class="card-body p-4">
                                <div class="flex items-center justify-between gap-4">
                                    <div class="flex-1 min-w-0">
                                        <div class="flex items-center gap-3 mb-2">
                                            <h3 class="font-mono text-sm font-semibold truncate">
                                                ${run.runId}
                                            </h3>
                                            <#if run.status == "running">
                                                <span class="badge badge-success badge-sm gap-1">
                                                    <span class="loading loading-spinner loading-xs"></span>
                                                    ${run.status}
                                                </span>
                                            <#elseif run.status == "passed">
                                                <span class="badge badge-info badge-sm">${run.status}</span>
                                            <#elseif run.status == "failed">
                                                <span class="badge badge-error badge-sm">${run.status}</span>
                                            <#else>
                                                <span class="badge badge-ghost badge-sm">${run.status}</span>
                                            </#if>
                                        </div>
                                        <div class="text-xs text-base-content/70 space-y-1">
                                            <#if run.scheduledAt?has_content>
                                                <div class="flex items-center gap-2">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                                    </svg>
                                                    <span>Scheduled: ${run.scheduledAt?datetime.iso}</span>
                                                </div>
                                            </#if>
                                            <#if run.initiatedAt?has_content>
                                                <div class="flex items-center gap-2">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                                    </svg>
                                                    <span>Started: ${run.initiatedAt?datetime.iso}</span>
                                                </div>
                                            </#if>
                                            <#if run.completedAt?has_content>
                                                <div class="flex items-center gap-2">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                                    </svg>
                                                    <span>Completed: ${run.completedAt?datetime.iso}</span>
                                                </div>
                                            </#if>
                                        </div>
                                    </div>
                                    <#if run.logger?has_content>
                                        <div class="flex-shrink-0">
                                            <a href="/logs?group=${group}&name=${name}&run=${run.runId}" class="btn btn-primary btn-sm gap-2">
                                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                                </svg>
                                                Logs
                                            </a>
                                        </div>
                                    </#if>
                                </div>
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
</@layout.page>
