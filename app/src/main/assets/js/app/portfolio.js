/* My portfolio on the Table page (v19). Button "💼 My portfolio" next to My data / Daily data.
   1) Portfolio PDF  -> opening holdings (Name, Qty, Avg cost) as of a date (preview + edit before saving)
   2) Buy/Sell confirmation PDF (file, or Gmail AUTO) -> trades added DATE-WISE on top of the opening holdings
      (trades dated on/before the opening date are ignored, they are already inside the portfolio PDF)
   3) ➤ Send to Cards -> addFile('My portfolio', Date/Type/Name/Quantity/Price rows) so Cards, Table and Compare
      count it like any other data. After the first send, every change re-sends silently.
   Gmail AUTO = Android.gmailFetch (IMAP + app password) -> window.__gmRes. AUTO and MANUAL are fallbacks of each other.
   Storage: ds_pf_v1 (portfolio), ds_gm_cfg (Gmail settings), ds_gm_seen. All ids/classes start with "gp"/"gm". */
(function(){try{
const g=id=>document.getElementById(id),sw=g('ltSw');if(!sw)return;
const LS=(k,v)=>{try{if(v===undefined)return JSON.parse(localStorage.getItem(k)||'null');localStorage.setItem(k,JSON.stringify(v))}catch(e){return null}};
const esc=s=>String(s).replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
const MN=['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],p2=n=>String(n).padStart(2,'0');
const dtxt=t=>{const d=new Date(t);return p2(d.getUTCDate())+'-'+MN[d.getUTCMonth()]+'-'+d.getUTCFullYear()};
const diso=t=>new Date(t).toISOString().slice(0,10);
const nz=s=>parseFloat(String(s).replace(/,/g,''));
const fm=(n,d)=>(+n).toLocaleString('en-US',{minimumFractionDigits:d===undefined?2:d,maximumFractionDigits:d===undefined?2:d});
let P=Object.assign({open:null,trades:[],sent:0},LS('ds_pf_v1')||{});
let C=Object.assign({user:'',pass:'',from:'',days:10,pdfpw:'',auto:0,last:0},LS('ds_gm_cfg')||{});
let seen=LS('ds_gm_seen')||[],busy=0,isOpen=false;
const saveP=()=>LS('ds_pf_v1',P),saveC=()=>LS('ds_gm_cfg',C);
const hasBridge=()=>!!(window.Android&&window.Android.gmailFetch),canAuto=()=>hasBridge()&&!!C.user&&!!C.pass;
const HDR=['Date','Type','Name','Quantity','Price'];

/* ---------- UI ---------- */
const st=document.createElement('style');st.textContent=
'#gpBox{border:1px solid var(--bd);border-radius:12px;padding:8px;margin:0 0 8px}#gpBox[hidden]{display:none}'+
'#gpBox input[type=text],#gpBox input[type=password],#gpBox input[type=email],#gpBox input[type=date],#gpBox textarea,#gpBox select{border:1px solid var(--bd);border-radius:9px;background:var(--card);color:var(--tx);padding:7px 8px;font:inherit;font-size:.82rem;min-width:0;width:100%;box-sizing:border-box}'+
'#gpBox textarea{min-height:110px}.gpsum{font-size:.82rem;margin:2px 0 6px}.gpmsg{font-size:.78rem;margin:2px 2px 6px;color:var(--mut)}.gpmsg.bad{color:var(--ld,#d1383d)}'+
'.gpt{width:100%;border-collapse:collapse;font-size:.8rem}.gpt th,.gpt td{padding:5px 6px;border-bottom:1px solid var(--bd);text-align:right}.gpt th:first-child,.gpt td:first-child{text-align:left}'+
'.gpt td.b{color:var(--lu,#0a8f4d)}.gpt td.s{color:var(--ld,#d1383d)}#gpBox details{margin:6px 0}#gpBox summary{cursor:pointer;font-size:.82rem;font-weight:600}'+
'#gpSum{display:none}#gpBox .gpbtns{display:flex;flex-wrap:nowrap;gap:3px;margin-bottom:6px;overflow-x:auto;scrollbar-width:none}#gpBox .gpbtns button{flex:0 0 auto;padding:7px 6px;font-size:.72rem;white-space:nowrap}#ltSw.gpopen button[data-lt=my].on{background:transparent;color:var(--ac)}#ltSw button{font-size:.9rem;padding-left:4px;padding-right:4px}'+
'.gpx{background:transparent;color:var(--ld,#d1383d);border:0;padding:0 4px}#ltSw button.gpon{background:var(--ac);color:#fff}';
document.head.appendChild(st);
sw.querySelector('button[data-lt="daily"]').insertAdjacentHTML('beforebegin','<button type="button" id="gpTg">Portfolio</button>');
sw.insertAdjacentHTML('afterend','<div id="gpBox" hidden>'+
'<div class="gpsum" id="gpSum"></div>'+
'<div class="gpbtns"><button type="button" id="gpPf" title="Portfolio PDF">Portfolio</button><button type="button" id="gpTr" title="Buy/Sell PDF">Buy/Sell</button><button type="button" class="alt" id="gpGm" title="Gmail" aria-label="Gmail">📧</button><button type="button" class="alt" id="gpSet" title="Gmail settings" aria-label="Gmail settings">⚙</button><button type="button" class="alt" id="gpSend" title="Send to Cards">➤ Cards</button><button type="button" class="alt" id="gpEd" title="Edit opening" aria-label="Edit opening">✏</button><button type="button" class="alt" id="gpRs" title="Reset" aria-label="Reset">🗑</button></div>'+
'<div class="gpmsg" id="gpMsg"></div>'+
'<div class="ltpanel" id="gpPv" hidden><b>Opening holdings</b><p class="ltnote">Check the shares, one per line: <b>NAME QTY AVG-COST</b>. Fix anything wrong, set the date of the portfolio statement, then Save. Buy/Sell PDFs dated after this date are added on top.</p>'+
'<textarea id="gpTx" placeholder="FAMILYTEX 100 2.15&#10;ONEBANKPLC 50 6.90" spellcheck="false"></textarea>'+
'<div class="ltrow" style="margin-top:6px"><label class="ltnote" style="margin:0;flex:1 1 100%">Portfolio date (as on)</label><input type="date" id="gpDt"></div>'+
'<div class="ltrow"><button type="button" id="gpSv">Save opening</button><button type="button" class="alt" id="gpCx">Cancel</button></div></div>'+
'<div class="ltpanel" id="gmPn" hidden><b>Gmail settings (for 📧 Gmail)</b>'+
'<p class="ltnote">Auto needs a Gmail <b>App password</b> (Google Account › Security › 2-Step Verification › App passwords). Saved only on this phone. If Auto is not set up or fails, the file picker opens; that always works.</p>'+
'<div class="ltrow"><input type="email" id="gmU" placeholder="Gmail address" autocomplete="off"></div><div class="ltrow"><input type="password" id="gmP" placeholder="App password (16 letters)" autocomplete="off"></div>'+
'<div class="ltrow"><input type="text" id="gmF" placeholder="Broker email, e.g. info@lbsbd.com (empty = any sender)" autocomplete="off"></div>'+
'<div class="ltrow"><select id="gmD" aria-label="Look back"><option value="3">Last 3 days</option><option value="10">Last 10 days</option><option value="30">Last 30 days</option></select></div>'+
'<div class="ltrow"><input type="password" id="gmW" placeholder="PDF password (if the PDFs have one)" autocomplete="off"></div>'+
'<label class="ltnote" style="display:flex;gap:6px;align-items:center"><input type="checkbox" id="gmA" style="width:auto"> Check Gmail when the app opens (every 6 h)</label>'+
'<div class="ltrow" style="margin-top:6px"><button type="button" id="gmSv">Save</button><button type="button" class="alt" id="gmCl">Close</button></div></div>'+
'<div id="gpTb"></div>'+
'<input type="file" id="gpFP" accept="application/pdf,.pdf" hidden><input type="file" id="gpFT" accept="application/pdf,.pdf" multiple hidden></div>');
const msg=(t,bad)=>{const m=g('gpMsg'),pv=!g('gpPv').hidden;
 if(!t||bad||pv){m.textContent=t||'';m.className='gpmsg'+(bad?' bad':'');m.style.display=t?'':'none'}
 else{m.textContent='';m.style.display='none';try{gnToast(t)}catch(e){}}};
const msgPick=(t)=>{msg(t,1);g('gpMsg').insertAdjacentHTML('beforeend',' <button type="button" class="alt sm" id="gpPick">🧾 Choose PDF</button>');g('gpPick').onclick=()=>pick('gpFT')};
function apply(){let on=false;try{on=curTab==='table'}catch(e){}
 const d=g('tabShow')&&g('tabShow').classList.contains('ltdaily');
 g('gpBox').hidden=!(on&&!d&&isOpen);g('gpTg').classList.toggle('gpon',on&&!d&&isOpen);sw.classList.toggle('gpopen',on&&!d&&isOpen)}

/* ---------- portfolio maths ---------- */
function calc(){
 const H={},warn=[];let skipped=0;
 const asof=P.open?pDate(P.open.date):null;
 if(P.open)P.open.rows.forEach(r=>{const h=H[r.code]||(H[r.code]={q:0,c:0});h.q+=r.qty;h.c+=r.qty*r.avg});
 const ts=P.trades.map((t,i)=>({t,i,ts:pDate(t.date)||0})).sort((a,b)=>a.ts-b.ts||a.i-b.i);
 const used=[];
 for(const x of ts){const t=x.t;
  if(asof!==null&&x.ts<=asof){skipped++;continue}
  used.push(t);
  const h=H[t.code]||(H[t.code]={q:0,c:0});
  if(t.type==='Buy'){h.q+=t.qty;h.c+=t.qty*t.rate}
  else{const sq=Math.min(t.qty,h.q);if(t.qty>h.q)warn.push(t.code+': sold '+t.qty+' but only '+h.q+' held (opening missing?)');const a=h.q?h.c/h.q:0;h.c-=a*sq;h.q-=sq}
 }
 const list=Object.keys(H).filter(k=>H[k].q>0).sort().map(k=>({code:k,q:H[k].q,avg:H[k].c/H[k].q,cost:H[k].c}));
 return{list,skipped,warn,used,ts};
}
function draw(){
 const c=calc(),o=P.open;
 g('gpSum').innerHTML=(o?'Opening: <b>'+esc(o.date)+'</b> ('+o.rows.length+' shares)':'Opening: <b>not set</b>')+' · Trades: <b>'+c.used.length+'</b>'+(c.skipped?' (+'+c.skipped+' older, ignored)':'')+' · Holding: <b>'+c.list.length+'</b> shares';
 let h='';
 if(c.list.length)h+='<table class="gpt"><thead><tr><th>Name</th><th>Qty</th><th>Avg</th><th>Cost</th></tr></thead><tbody>'+c.list.map(r=>'<tr><td>'+esc(r.code)+'</td><td>'+fm(r.q,0)+'</td><td>'+fm(r.avg)+'</td><td>'+fm(r.cost)+'</td></tr>').join('')+'<tr><th>Total</th><th>'+fm(c.list.reduce((a,r)=>a+r.q,0),0)+'</th><th></th><th>'+fm(c.list.reduce((a,r)=>a+r.cost,0))+'</th></tr></tbody></table>';
 else h+='<div class="ltnote">No shares yet. Add the Portfolio PDF first, then Buy/Sell PDFs.</div>';
 if(c.warn.length)h+='<div class="gpmsg bad">'+c.warn.map(esc).join('<br>')+'</div>';
 if(P.trades.length){
  const rows=c.ts.slice().reverse().map(x=>{const t=x.t,old=c.used.indexOf(t)<0;return '<tr style="'+(old?'opacity:.45':'')+'"><td>'+esc(t.date)+'</td><td class="'+(t.type==='Buy'?'b':'s')+'">'+t.type+'</td><td style="text-align:left">'+esc(t.code)+'</td><td>'+t.qty+'</td><td>'+fm(t.rate)+'</td><td><button type="button" class="gpx" data-del="'+x.i+'" aria-label="Delete">✕</button></td></tr>'}).join('');
  h+='<details><summary>Date-wise Buy/Sell ('+P.trades.length+')</summary><table class="gpt"><thead><tr><th>Date</th><th>Type</th><th style="text-align:left">Name</th><th>Qty</th><th>Rate</th><th></th></tr></thead><tbody>'+rows+'</tbody></table></details>';
 }
 g('gpTb').innerHTML=h;
}

/* ---------- send to Cards ---------- */
function cardRowsOut(){
 const c=calc(),out=[];
 if(P.open)P.open.rows.forEach(r=>out.push([P.open.date,'Buy',r.code,String(r.qty),String(r.avg)]));
 /* Same share + same date + same Buy/Sell = ONE card (qty added, price = weighted average) */
 const G={},ord=[];
 c.ts.filter(x=>c.used.indexOf(x.t)>=0).forEach(x=>{const t=x.t,k=t.date+'|'+t.type+'|'+t.code;
  if(!G[k]){G[k]={date:t.date,type:t.type,code:t.code,q:0,v:0};ord.push(k)}
  G[k].q+=t.qty;G[k].v+=t.qty*t.rate});
 ord.forEach(k=>{const g=G[k];out.push([g.date,g.type,g.code,String(g.q),String(Math.round(g.v/g.q*10000)/10000)])});
 return out}
function sendCards(silent){
 const r=cardRowsOut();
 if(!r.length){if(!silent)msg('Nothing to send yet.',1);return}
 addFile('My portfolio',[HDR,...r],{size:0});P.sent=1;saveP();
 if(!silent){msg('✓ Sent to Cards ('+r.length+' rows). Later Buy/Sell PDFs update it automatically.');try{setTab('show')}catch(e){}}
}
const changed=()=>{saveP();draw();if(P.sent)try{sendCards(true)}catch(e){}};

/* ---------- PDF reading ---------- */
const b64u8=b=>{const s=atob(b),u=new Uint8Array(s.length);for(let i=0;i<s.length;i++)u[i]=s.charCodeAt(i);return u};
async function pdfRead(buf){
 await need('pdf');let pdf;
 try{pdf=await pdfjsLib.getDocument({data:buf,password:C.pdfpw||undefined}).promise}
 catch(e){if(e&&e.name==='PasswordException')throw new Error('This PDF has a password. Put it in ⚙ › PDF password.');throw e}
 const cells=[];
 for(let p=1;p<=pdf.numPages;p++){const tc=await(await pdf.getPage(p)).getTextContent();toRows(tc.items).forEach(r=>{r=r.map(x=>String(x).replace(/\s+/g,' ').trim()).filter(Boolean);if(r.length)cells.push(r)})}
 return{cells,lines:cells.map(r=>r.join(' '))};
}
const isConf=lines=>/TRADE\s+CONFIRMATION/i.test(lines.join(' '));
function parseTrade(lines){
 const o={client:'',date:'',conf:'',trades:[]};let side=null,m;
 const row=/^([A-Z][A-Z0-9&._-]+)\s+(\d+)\s+([\d,]+\.\d+)\s+([\d,]+\.\d+)\s+([\d,]+\.\d+)\s+([\d,]+\.\d+)$/;
 for(const l of lines){
  if(!o.client&&(m=l.match(/Client\s*Cod\w*\s+([A-Z0-9]+)/i)))o.client=m[1];
  if(!o.date&&(m=l.match(/Trading\s*Date:?\s*(?:From\s*)?(\d{1,2}-[A-Za-z]{3}-\d{4})/i)))o.date=m[1];
  if(!o.conf&&(m=l.match(/Confirmation\s*N\w*\s+(\d+)\s*-\s*(\d+)/i)))o.conf=m[1]+'-'+m[2];
  if(/^(Buy|Sale|Sell)$/i.test(l)){side=/^buy$/i.test(l)?'Buy':'Sell';continue}
  if(side&&(m=l.match(row)))o.trades.push({type:side,code:m[1],qty:+m[2],rate:nz(m[3])});
 }
 return o}
/* a Buy/Sell confirmation -> trades (same confirmation twice = replaced, never doubled) */
function addConfirmation(p,fname){
 if(!p.trades.length)throw new Error((fname||'PDF')+': no Buy/Sale rows found');
 const date=p.date||dtxt(Date.now()),key=(p.client||'')+'|'+date+'|'+(p.conf||fname||'');
 P.trades=P.trades.filter(t=>t.conf!==key);
 p.trades.forEach(t=>P.trades.push({date,type:t.type,code:t.code,qty:t.qty,rate:t.rate,conf:key}));
 const b=p.trades.filter(t=>t.type==='Buy').length;
 return date+': '+b+' buy, '+(p.trades.length-b)+' sell ('+p.trades.map(t=>t.code+' '+(t.type==='Buy'?'+':'-')+t.qty).join(', ')+')';
}
/* portfolio statement -> proposed opening rows. Layout is detected by data pattern (code + numbers), so it needs no fixed columns. */
const SKIP=/^(TOTAL|GRAND|SUB|PAGE|CLIENT|NAME|BO|DATE|PRINTED|BALANCE|CASH|LEDGER|MARKET|PORTFOLIO|SECTOR|INSTRUMENT|SCRIP|CODE|SL|NO|ID|PHONE|ADDRESS)\b/i;
function parsePortfolio(cells){
 {const lb=parsePortfolioLB(cells);if(lb)return lb}
 let asof=null,hdr=null;
 for(const r of cells){const l=r.join(' ');
  const m=l.match(/(?:as\s*(?:on|of|at)|up\s*to|till|statement\s*date|report\s*date|date)\s*[:\-]?\s*(\d{1,2}[-\/. ][A-Za-z]{3}[a-z]*[-\/., ]+\d{2,4}|\d{1,2}[-\/.]\d{1,2}[-\/.]\d{2,4}|\d{4}-\d{1,2}-\d{1,2})/i);
  if(m&&pDate(m[1])!==null&&asof===null)asof=pDate(m[1]);
  if(!hdr&&r.length>=3&&r.filter(c=>/qty|quantity|avg|average|cost|rate|price|value|balance|instrument|scrip|symbol|company/i.test(c)&&isNaN(nz(c))).length>=3)hdr=r}
 const out=[];
 for(const r of cells){
  if(r===hdr)continue;
  const ci=r.findIndex(c=>/^[A-Za-z][A-Za-z0-9&._-]{2,24}$/.test(c)&&!SKIP.test(c)&&isNaN(nz(c)));
  if(ci<0)continue;
  const nums=r.slice(ci+1).filter(c=>/^-?[\d,]*\.?\d+$/.test(c)&&c!=='').map(nz);
  if(nums.length<2)continue;
  let qi=-1,ai=-1;
  if(hdr&&hdr.length===r.length){
   const L=hdr.map(x=>x.toLowerCase());
   const qn=L.findIndex((x,i)=>i>ci&&/qty|quantity|shares|balance|holding/.test(x)),an=L.findIndex((x,i)=>i>ci&&/avg|average/.test(x))>=0?L.findIndex((x,i)=>i>ci&&/avg|average/.test(x)):L.findIndex((x,i)=>i>ci&&/cost.*(rate|price)|buy.*(rate|price)|rate/.test(x));
   if(qn>=0&&an>=0){const q=nz(r[qn]),a=nz(r[an]);if(!isNaN(q)&&!isNaN(a)){out.push({code:r[ci].toUpperCase(),qty:q,avg:a});continue}}
  }
  const q=nums.findIndex(n=>n>=1&&Math.round(n)===n);
  if(q<0)continue;
  const a=nums.slice(q+1).find(n=>n>0&&Math.round(n*100)/100===n&&n<100000);
  out.push({code:r[ci].toUpperCase(),qty:nums[q],avg:a===undefined?0:a});
 }
 const m={};out.forEach(o=>{const k=m[o.code];if(k){k.avg=(k.avg*k.qty+o.avg*o.qty)/(k.qty+o.qty);k.qty+=o.qty}else m[o.code]={code:o.code,qty:o.qty,avg:o.avg}});
 return{asof,rows:Object.values(m)};
}
/* LankaBangla "CLIENT WISE PORTFOLIO STATEMENT" (verified on a real PDF): row = CODE CAT SALEABLE LIEN LOCK TOTAL-QTY COST-PRICE COST-AMT MKT-PRICE MKT-VALUE %PORT GAIN %GAIN.
   Qty = Total Qty (it includes shares bought today that are not yet saleable), avg = cost amount / total qty. The date used is "Printed On" (the
   "As On" line of this statement can show a later day); the total cost line is checked against the sum of the rows. */
function parsePortfolioLB(cells){
 const re=/^([A-Z0-9][A-Z0-9&._()\-]*)\s+([A-Z])\s+([\d,]+)\s+\d+\s+\d+\s+([\d,]+)\s+([\d,]+\.\d+)\s+([\d,]+\.\d+)\s+[\d,]+\.\d+\s+[\d,]+\.\d+\s+\d+\.\d+\s+-?[\d,]+\.\d+\s+-?[\d,]+\.\d+$/;
 let printed=null,ason=null,tot=null,sum=0;const rows=[];let m;
 for(const r of cells){const l=r.join(' ');
  if(printed===null&&(m=l.match(/Printed\s*On:?\s*(\d{1,2}-[A-Za-z]{3}-\d{4})/i)))printed=pDate(m[1]);
  if(ason===null&&(m=l.match(/As\s*On:?\s*(\d{1,2}-[A-Za-z]{3}-\d{4})/i)))ason=pDate(m[1]);
  if((m=l.match(re))){const q=nz(m[4]),pr=nz(m[5]),am=nz(m[6]);
   rows.push({code:m[1],qty:q,avg:(q>0&&Math.abs(am/q-pr)<=pr*0.01+0.005)?am/q:pr});sum+=am;continue}
  if((m=l.match(/^Total:?\s+([\d,]+\.\d+)\s+[\d,]+\.\d+\s+100\b/)))tot=nz(m[1]);
 }
 if(!rows.length)return null;
 const asof=printed!==null?printed:ason;
 let note='Found '+rows.length+' shares';
 if(tot!==null)note+=Math.abs(tot-sum)<1?' · total cost matches the PDF ('+fm(tot)+') ✓':' · total cost differs from the PDF ('+fm(sum)+' vs '+fm(tot)+'), check the list';
 if(printed!==null&&ason!==null&&printed!==ason)note+=' · date = printed '+dtxt(printed)+' (statement says As On '+dtxt(ason)+')';
 return{asof,rows,note:note+'. Check, then Save.'};
}
const rowsToText=rows=>rows.map(r=>r.code+' '+r.qty+' '+(+r.avg.toFixed(4))).join('\n');
function textToRows(t){
 const rows=[],bad=[];
 t.split(/\r?\n/).forEach(l=>{l=l.trim();if(!l)return;
  const m=l.match(/^([A-Za-z0-9][A-Za-z0-9&._()-]*)[\s,;\t]+([\d,]+(?:\.\d+)?)(?:[\s,;\t]+([\d,]+(?:\.\d+)?))?$/);
  if(!m)bad.push(l);else rows.push({code:m[1].toUpperCase(),qty:nz(m[2]),avg:m[3]?nz(m[3]):0})});
 return{rows,bad}}
function showPreview(rows,asof,note){
 g('gpPv').hidden=false;g('gpTx').value=rowsToText(rows);
 g('gpDt').value=asof!==null&&asof!==undefined?diso(asof):(P.open?diso(pDate(P.open.date)):'');
 if(note)msg(note,rows.length?0:1);g('gpPv').scrollIntoView({block:'nearest'})}

/* ---------- file handlers ---------- */
async function onTradeFile(f){
 const d=await pdfRead(await f.arrayBuffer());
 if(!isConf(d.lines))throw new Error(f.name+': this is not a Buy/Sell confirmation PDF (use 📕 Portfolio PDF for the portfolio statement).');
 return addConfirmation(parseTrade(d.lines),f.name)}
async function onPortfolioFile(f){
 const d=await pdfRead(await f.arrayBuffer());
 if(isConf(d.lines)){const t=addConfirmation(parseTrade(d.lines),f.name);changed();msg('This was a Buy/Sell confirmation, added as a trade: '+t);return}
 const r=parsePortfolio(d.cells);
 showPreview(r.rows,r.asof,r.rows.length?(r.note||'Found '+r.rows.length+' shares. Check them, set the date, then Save.'):'Could not read the shares from this PDF. Type them below (NAME QTY AVG-COST per line) and Save.');
}
function pick(id){const i=g(id);i.value='';try{i.click()}catch(e){}}

/* ---------- Gmail AUTO ---------- */
const cbs={};let seq=0;
window.__gmRes=(id,ok,p)=>{const c=cbs[id];delete cbs[id];if(!c)return;try{c(ok,new TextDecoder('utf-8').decode(b64u8(p)))}catch(e){c(false,String(e&&e.message||e))}};
function bridgeFetch(){return new Promise((res,rej)=>{
 const id='gm'+(++seq);
 const tm=setTimeout(()=>{delete cbs[id];rej(new Error('Gmail did not answer (time out)'))},90000);
 cbs[id]=(ok,t)=>{clearTimeout(tm);if(!ok)return rej(new Error(t||'Gmail error'));try{res(JSON.parse(t))}catch(e){rej(new Error('Gmail answer unreadable'))}};
 try{window.Android.gmailFetch(C.user,C.pass,C.from||'',+C.days||10,id)}catch(e){clearTimeout(tm);delete cbs[id];rej(e)}})}
async function runAuto(silent){
 if(!hasBridge())throw new Error('Gmail Auto needs the new APK (this one has no Gmail support).');
 if(!C.user||!C.pass)throw new Error('Gmail address / app password is not set (⚙).');
 msg('Checking Gmail…');
 let list;try{list=await bridgeFetch()}finally{C.last=Date.now();saveC()}
 const key=a=>(a.name||'')+'|'+(a.date||'')+'|'+(a.id||'');
 if(silent)list=list.filter(a=>!seen.includes(key(a)));
 if(!list.length)throw new Error(silent?'No new PDF in Gmail.':'No PDF mail found'+(C.from?' from '+C.from:'')+' in the last '+C.days+' days.');
 const ok=[],bad=[];let other=0;
 for(const a of list){
  try{const d=await pdfRead(b64u8(a.b64).buffer);
   if(!isConf(d.lines)){other++;seen.push(key(a));continue}
   ok.push(addConfirmation(parseTrade(d.lines),a.name));seen.push(key(a))}
  catch(e){bad.push((e&&e.message)||String(e))}
 }
 if(seen.length>200)seen=seen.slice(-200);LS('ds_gm_seen',seen);
 if(ok.length)changed();
 if(!ok.length)throw new Error(bad[0]||(other?other+' PDF found but none is a Buy/Sell confirmation.':'Could not read the PDF from Gmail.'));
 return '✓ Gmail: '+ok.join(' · ')+(bad.length?' · '+bad.length+' failed':'')+(other?' · '+other+' other PDF skipped':'');
}
async function go(){
 if(busy)return;busy=1;g('gpGm').disabled=true;
 try{msg(await runAuto(false))}
 catch(e){msgPick('Gmail: '+((e&&e.message)||e)+' → choose the Buy/Sell PDF by hand.');pick('gpFT')}
 busy=0;g('gpGm').disabled=false}

/* ---------- events ---------- */
sw.addEventListener('click',e=>{const b=e.target.closest('button');if(!b)return;
 if(b.id==='gpTg'){isOpen=!isOpen;if(isOpen){draw();let m='my';try{m=localStorage.getItem('ds_lt_mode')}catch(x){}if(m&&m.indexOf('daily')>=0){const my=sw.querySelector('button[data-lt="my"]');if(my)my.click()}}}
 else if(b.dataset.lt)isOpen=false;
 setTimeout(apply,0)});
g('gpPf').onclick=()=>pick('gpFP');
g('gpTr').onclick=()=>pick('gpFT');
g('gpGm').onclick=go;
g('gpFP').onchange=async e=>{const f=(e.target.files||[])[0];if(!f||busy)return;busy=1;msg('Reading portfolio PDF…');
 try{await onPortfolioFile(f)}catch(er){showPreview([],null);msg(((er&&er.message)||er)+' You can type the shares below.',1)}busy=0};
g('gpFT').onchange=async e=>{const fs=Array.from(e.target.files||[]);if(!fs.length||busy)return;busy=1;msg('Reading PDF…');
 const ok=[],bad=[];
 for(const f of fs){try{ok.push(await onTradeFile(f))}catch(er){bad.push((er&&er.message)||String(er))}}
 if(ok.length){changed();msg('✓ '+ok.join(' · ')+(bad.length?' · '+bad.length+' failed ('+bad[0]+')':''))}
 else if(canAuto()){msg('Could not read the file ('+(bad[0]||'?')+'). Trying Gmail…',1);try{msg(await runAuto(false))}catch(er){msg('PDF: '+(bad[0]||'failed')+' · Gmail: '+er.message,1)}}
 else msg(bad[0]||'Could not read the PDF.',1);
 busy=0};
g('gpEd').onclick=()=>showPreview(P.open?P.open.rows:[],P.open?pDate(P.open.date):null,P.open?'':'Type the shares below (NAME QTY AVG-COST per line), set the date and Save.');
g('gpSv').onclick=()=>{
 const r=textToRows(g('gpTx').value),dv=g('gpDt').value;
 if(!r.rows.length){msg('Write at least one line: NAME QTY AVG-COST'+(r.bad.length?' (cannot read: '+r.bad[0]+')':''),1);return}
 if(!dv){msg('Set the portfolio date first.',1);return}
 const t=Date.UTC(+dv.slice(0,4),+dv.slice(5,7)-1,+dv.slice(8,10));
 P.open={date:dtxt(t),rows:r.rows};g('gpPv').hidden=true;changed();
 msg('✓ Opening saved: '+r.rows.length+' shares as on '+P.open.date+(r.bad.length?' · '+r.bad.length+' line(s) skipped':''))};
g('gpCx').onclick=()=>{g('gpPv').hidden=true};
g('gpSend').onclick=()=>sendCards(false);
let rsT=0;
g('gpRs').onclick=()=>{if(Date.now()-rsT<4000){P={open:null,trades:[],sent:0};saveP();draw();msg('Portfolio cleared (the "My portfolio" entry in Cards stays until you delete it there).');g('gpRs').textContent='🗑 Reset';rsT=0}else{rsT=Date.now();g('gpRs').textContent='Tap again';setTimeout(()=>{g('gpRs').textContent='🗑 Reset';rsT=0},4000)}};
g('gpTb').addEventListener('click',e=>{const b=e.target.closest('[data-del]');if(!b)return;P.trades.splice(+b.dataset.del,1);changed()});
const fillC=()=>{g('gmU').value=C.user;g('gmP').value=C.pass;g('gmF').value=C.from;g('gmD').value=String(C.days);g('gmW').value=C.pdfpw;g('gmA').checked=!!C.auto};
g('gpSet').onclick=()=>{const p=g('gmPn');p.hidden=!p.hidden;if(!p.hidden)fillC()};
g('gmCl').onclick=()=>{g('gmPn').hidden=true};
g('gmSv').onclick=()=>{C.user=g('gmU').value.trim();C.pass=g('gmP').value.replace(/\s+/g,'');C.from=g('gmF').value.trim();C.days=+g('gmD').value||10;C.pdfpw=g('gmW').value;C.auto=g('gmA').checked?1:0;saveC();g('gmPn').hidden=true;msg('Saved.')};
if(typeof setTab==='function'){const o=window.setTab;window.setTab=function(){const r=o.apply(this,arguments);try{apply()}catch(e){}return r}}
draw();apply();

/* auto check when the app opens (silent: only a status line, no file picker) */
if(C.auto&&canAuto()&&Date.now()-(C.last||0)>6*3600e3){
 setTimeout(async()=>{if(busy)return;busy=1;try{msg(await runAuto(true))}catch(e){msg('Gmail auto: '+((e&&e.message)||e),1)}busy=0},4000)}
}catch(err){console.error('portfolio',err)}})();
