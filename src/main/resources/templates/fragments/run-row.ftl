<div class="rounded-lg border border-base-300 bg-base-100 hover:border-base-content/20 transition-colors duration-150 p-4 run-row"
     id="run-${run.runId}">
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
        <div class="flex items-center gap-2">
            <#if run.status == "running">
                <button class="btn btn-ghost btn-xs" title="Stop run"
                        data-on-click="@post('/stop?run=${run.runId}')">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <rect x="6" y="6" width="12" height="12" rx="1" fill="currentColor" stroke="none"/>
                    </svg>
                </button>
            </#if>
            <#if run.logger?has_content>
                <a class="btn btn-ghost btn-sm"
                   href="/logs?group=${run.group}&name=${run.name}&run=${run.runId}">
                    Logs
                </a>
            </#if>
        </div>
    </div>
</div>
