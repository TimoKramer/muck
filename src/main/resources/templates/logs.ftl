<#import "layout.ftl" as layout>

<@layout.page title="Logs - Muck">
    <div class="card bg-base-100 shadow-2xl">
        <div class="card-body">
            <div class="flex items-center justify-between mb-4">
                <a href="/runs?group=${group}&name=${name}" class="btn btn-ghost btn-sm gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to Runs
                </a>
                <#if connected>
                    <div class="flex items-center gap-2 px-3 py-1 rounded-full bg-green-100 text-green-700 text-sm" title=${bobUrl}>
                        <span class="relative flex h-2 w-2">
                            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                            <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                        </span>
                        Connected
                    </div>
                <#else>
                    <div class="flex items-center gap-2 px-3 py-1 rounded-full bg-red-100 text-red-700 text-sm">
                        <span class="relative flex h-2 w-2">
                            <span class="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
                        </span>
                        Disconnected
                    </div>
                </#if>
            </div>

            <div class="mb-4">
                    <h2 class="card-title text-3xl mb-4">Logs</h2>
                    <h3 class="text-xl font-bold font-mono">${run}</h2>
                    <div class="text-sm opacity-60 mt-1">Run Logs</div>
                </div>
            </div>

            <div class="form-control w-full">
                <textarea
                    id="logs-output"
                    class="textarea textarea-bordered w-full font-mono text-sm bg-base-200"
                    style="min-height: 500px; resize: vertical;"
                    readonly
                    placeholder="Loading logs..."></textarea>
            </div>

            <div id="logs-status" class="text-sm opacity-60 mt-2">
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
