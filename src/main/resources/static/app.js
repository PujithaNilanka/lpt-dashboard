const tableBody = document.querySelector('#resultsTable tbody');
const status = document.getElementById('status');
let currentData = [];
let chart;

document.getElementById('search').addEventListener('click', fetchResults);

// make columns sortable
document.querySelectorAll('#resultsTable th').forEach(th => {
  th.addEventListener('click', () => {
    const field = th.dataset.field;
    sortBy(field);
    renderTable(currentData);
  });
});

function setStatus(msg) { status.textContent = msg; }

async function fetchResults() {
  const projectName = document.getElementById('projectName').value;
  const testEnv = document.getElementById('testEnv').value;
  const testType = document.getElementById('testType').value;
  const limit = document.getElementById('limit').value;

  if (!projectName || !testEnv || !testType) {
    setStatus('Please provide projectName, testEnv and testType prefix.');
    return;
  }å
  setStatus('Loading...');
  try {
    const qs = new URLSearchParams({ projectName, testEnv, testTypePrefix: testType, limit });
    const res = await fetch('/api/results?' + qs.toString());
    if (!res.ok) throw new Error('Request failed: ' + res.status);
    const items = await res.json();
    currentData = items;
    setStatus(`Loaded ${items.length} items`);
    renderTable(items);
    renderChart(items);
  } catch (err) {
    setStatus('Error: ' + err.message);
  }
}

function renderTable(items) {
  tableBody.innerHTML = '';
  items.forEach(it => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${escapeHtml(it.testResultId)}</td>
                    <td>${escapeHtml(it.testDateTime)}</td>
                    <td>${num(it.errorRate)}</td>
                    <td>${num(it.averageResponseTime)}</td>
                    <td>${num(it.throughput)}</td>`;
    tableBody.appendChild(tr);
  });
}

function renderChart(items) {
  // sort by datetime
  const sorted = [...items].sort((a,b)=> (a.testDateTime||'').localeCompare(b.testDateTime||''));
  const labels = sorted.map(i => i.testDateTime || '');
  const errorRate = sorted.map(i => i.errorRate ?? null);
  const avgResp = sorted.map(i => i.averageResponseTime ?? null);
  const throughput = sorted.map(i => i.throughput ?? null);

  const ctx = document.getElementById('trendChart').getContext('2d');
  if (chart) chart.destroy();
  chart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        { label: 'Error Rate', data: errorRate, yAxisID: 'y1', fill:false },
        { label: 'Avg Resp Time', data: avgResp, yAxisID: 'y2', fill:false },
        { label: 'Throughput', data: throughput, yAxisID: 'y3', fill:false }
      ]
    },
    options: {
      interaction: { mode: 'index', intersect: false },
      stacked: false,
      scales: {
        x: { display: true, title: { display: true, text: 'testDateTime' } },
        y1: { type: 'linear', display: true, position: 'left', title: { display: true, text: 'Error Rate' } },
        y2: { type: 'linear', display: true, position: 'right', title: { display: true, text: 'Avg Resp Time' }, grid: { drawOnChartArea: false } },
        y3: { type: 'linear', display: true, position: 'right', title: { display: true, text: 'Throughput' }, grid: { drawOnChartArea: false }, offset: true }
      }
    }
  });
}

function escapeHtml(s) {
  if (s === null || s === undefined) return '';
  return String(s).replace(/[&<>"']/g, function (m) {
    return ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' })[m];
  });
}
function num(v) { return v === null || v === undefined ? '' : v; }

function sortBy(field) {
  currentData.sort((a,b)=>{
    const av = a[field], bv = b[field];
    if (av == null && bv == null) return 0;
    if (av == null) return 1;
    if (bv == null) return -1;
    if (!isNaN(Number(av)) && !isNaN(Number(bv))) {
      return Number(av) - Number(bv);
    }
    return String(av).localeCompare(String(bv));
  });
}
