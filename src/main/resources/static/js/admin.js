// admin.js
// Lightweight interactive behavior and charts for admin dashboard.
// No backend; stub actions simulate expected flows.

// ===== sample data (should be replaced by real backend responses) =====
const sample = {
  totals: { users: 3240, sellers: 412, verifiedSellers: 298, listings: 2134, flagged: 12, sales: 1824 },
  monthlyListings: [90,120,150,200,180,220,240,210,200,230,260,280],
  verificationQueue:[
    {id:101,name:"Aisha B",studentId:"UMT001",submitted:"2025-11-23",doc:"id_aisha.png"},
    {id:102,name:"Ibrahim S",studentId:"UMT089",submitted:"2025-11-25",doc:"id_ibrahim.png"}
  ],
  flaggedProducts:[
    {id:501,title:"Used Laptop",seller:"Ibrahim",score:0.87,reason:"Possible fraudulent images",image:"/static/images/placeholder-product.png"},
    {id:502,title:"Designer Bag",seller:"Ali",score:0.92,reason:"Price mismatch",image:"/static/images/placeholder-product.png"}
  ],
  categories:[
    {name:"Books",count:410},{name:"Electronics",count:320},{name:"Fashion",count:220},
    {name:"Food",count:180},{name:"Stationery",count:120}
  ]
};

// ===== populate stat cards =====
document.addEventListener('DOMContentLoaded',function(){
  document.getElementById('statUsers').textContent = sample.totals.users.toLocaleString();
  document.getElementById('statSellers').textContent = sample.totals.sellers;
  document.getElementById('statVerified').textContent = sample.totals.verifiedSellers;
  document.getElementById('statListings').textContent = sample.totals.listings;
  document.getElementById('statFlagged').textContent = sample.totals.flagged;
  document.getElementById('statSales').textContent = sample.totals.sales;

  renderVerificationTable();
  renderFlaggedTable();
  renderCategoryGrid();
  renderCharts();
});

// ===== render verification table =====
function renderVerificationTable(){
  const tbody = document.querySelector('#verificationTable tbody');
  sample.verificationQueue.forEach(item=>{
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${item.submitted}</td>
      <td>${item.name}</td>
      <td>${item.studentId}</td>
      <td><a href="${item.doc}" target="_blank">View Document</a></td>
      <td class="actions">
        <button class="btn small" onclick="approveSeller(${item.id})">Approve</button>
        <button class="btn outline small" onclick="rejectSeller(${item.id})">Reject</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// ===== render flagged table =====
function renderFlaggedTable(){
  const tbody = document.querySelector('#flaggedTable tbody');
  sample.flaggedProducts.forEach(p=>{
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><input type="checkbox" /></td>
      <td><div style="display:flex;gap:12px;align-items:center"><img src="${p.image}" alt="${p.title}"><div><strong>${p.title}</strong><div class="muted small">ID: ${p.id}</div></div></div></td>
      <td>${p.seller}</td>
      <td><div class="pill" style="background:rgba(255,199,0,0.12);color:#b45309">${(p.score*100).toFixed(0)}%</div></td>
      <td>${p.reason}</td>
      <td class="actions">
        <button class="btn" onclick="approveListing(${p.id})">Approve</button>
        <button class="btn outline" onclick="deleteListing(${p.id})">Delete</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// ===== categories grid =====
function renderCategoryGrid(){
  const grid = document.getElementById('categoryGrid');
  sample.categories.forEach(c=>{
    const card = document.createElement('div');
    card.className='category-card';
    card.innerHTML = `<div><h4>${c.name}</h4><div class="muted small">${c.count} listings</div></div><div><button class="btn small">Edit</button></div>`;
    grid.appendChild(card);
  });
}

// ===== simple actions (simulate network & show toast) =====
function showToast(msg, kind='info'){
  const el = document.createElement('div');
  el.className = 'toast ' + kind;
  el.textContent = msg;
  document.body.appendChild(el);
  setTimeout(()=>el.classList.add('visible'),30);
  setTimeout(()=>el.classList.remove('visible'),3200);
  setTimeout(()=>el.remove(),3600);
}
function approveSeller(id){
  showToast('Seller approved (id: '+id+')', 'success');
}
function rejectSeller(id){
  showToast('Seller rejected (id: '+id+')', 'danger');
}
function approveListing(id){
  showToast('Listing approved (id: '+id+')', 'success');
}
function deleteListing(id){
  showToast('Listing deleted (id: '+id+')', 'danger');
}

// ===== charts =====
function renderCharts(){
  const ctx = document.getElementById('chartListings').getContext('2d');
  new Chart(ctx, {
    type:'line',
    data:{
      labels:['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
      datasets:[{
        label:'Listings',
        data: sample.monthlyListings,
        borderColor:'#1f2937',
        backgroundColor:'rgba(31,41,55,0.06)',
        tension:0.35,
        pointRadius:3,
        pointBackgroundColor:'#111827',
        fill:true
      }]
    },
    options:{
      responsive:true,
      plugins:{legend:{display:false}},
      scales:{
        x:{grid:{display:false}},
        y:{beginAtZero:true}
      }
    }
  });

  const ctx2 = document.getElementById('chartVerification').getContext('2d');
  const verified = sample.totals.verifiedSellers;
  const notVerified = sample.totals.sellers - sample.totals.verifiedSellers;
  new Chart(ctx2, {
    type:'doughnut',
    data:{
      labels:['Verified','Not Verified'],
      datasets:[{
        data:[verified, notVerified],
        backgroundColor:[ 'rgba(34,197,94,0.9)','rgba(245,158,11,0.9)']
      }]
    },
    options:{plugins:{legend:{position:'bottom'}}}
  });
}

// ===== small toast CSS injection (runtime) =====
const styleToast = document.createElement('style');
styleToast.textContent = `
.toast{position:fixed;right:20px;bottom:20px;background:#111827;color:white;padding:10px 14px;border-radius:10px;opacity:0;transform:translateY(6px);transition:all 280ms;z-index:9999}
.toast.visible{opacity:1;transform:none}
.toast.success{background:var(--success)}
.toast.danger{background:var(--danger)}
`;
document.head.appendChild(styleToast);
