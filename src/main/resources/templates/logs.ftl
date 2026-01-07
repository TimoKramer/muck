<#import "layout.ftl" as layout>

<@layout.page title="Logs - Muck">
    <div class="card bg-base-100 shadow-sm">
        <div class="card-body">
            <div class="flex items-center justify-between mb-4">
                <a href="/runs?group=${group}&name=${name}" class="btn btn-ghost btn-sm gap-1">
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
                <h2 class="text-xl font-semibold">Logs</h2>
                <div class="font-mono text-sm text-base-content/60 mt-1">${run}</div>
            </div>

            <textarea
                id="logs-output"
                class="w-full font-mono text-xs bg-base-200 rounded-lg p-4 border border-base-300"
                style="min-height: 500px; resize: vertical;"
                readonly
                placeholder="Loading logs..."></textarea>

            <div id="logs-status" class="text-sm text-base-content/60 mt-2">
                <span class="loading loading-spinner loading-xs"></span>
                Connecting...
            </div>
        </div>
    </div>

    <script>
        (function() {
            const textarea = document.getElementById('logs-output');
            const status = document.getElementById('logs-status');
            const runId = '${run?js_string}';

            fetch('/logs/stream?run=' + encodeURIComponent(runId))
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch logs: ' + response.status);
                    }

                    status.innerHTML = '<span class="text-success">Streaming...</span>';

                    const reader = response.body.getReader();
                    const decoder = new TextDecoder();

                    function read() {
                        reader.read().then(({done, value}) => {
                            if (done) {
                                status.innerHTML = '<span class="text-info">Stream completed</span>';
                                return;
                            }

                            const text = decoder.decode(value, {stream: true});
                            textarea.value += text;
                            textarea.scrollTop = textarea.scrollHeight;

                            read();
                        }).catch(err => {
                            status.innerHTML = '<span class="text-error">Error: ' + err.message + '</span>';
                        });
                    }

                    read();
                })
                .catch(err => {
                    status.innerHTML = '<span class="text-error">Error: ' + err.message + '</span>';
                    textarea.placeholder = 'Failed to load logs';
                });
        })();
    </script>
</@layout.page>
