<#if status == "running">
    <span class="badge badge-outline badge-sm gap-1 text-info border-info">
        <span class="loading loading-spinner loading-xs"></span>
        ${status}
    </span>
<#elseif status == "passed">
    <span class="badge badge-outline badge-sm text-success border-success">${status}</span>
<#elseif status == "failed">
    <span class="badge badge-outline badge-sm text-error border-error">${status}</span>
<#elseif status == "stopped">
    <span class="badge badge-outline badge-sm text-warning border-warning">${status}</span>
<#elseif status == "paused">
    <span class="badge badge-outline badge-sm text-warning border-warning">${status}</span>
<#else>
    <span class="badge badge-outline badge-sm">${status}</span>
</#if>
