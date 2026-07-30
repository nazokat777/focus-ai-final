/* AI Coach — xavfsiz ko'prik (Vercel Serverless Function), Groq (bepul).
   Kalit FAQAT serverda (process.env.GROQ_KEY) — brauzerga hech qachon tushmaydi.
   Ilova POST /api/coach {message, context} yuboradi, javob {text} qaytadi. */

/* Groq bepul modellari — biri ishlamasa (429/404) keyingisiga o'tadi */
const MODELS = ['llama-3.3-70b-versatile', 'llama-3.1-8b-instant', 'gemma2-9b-it'];
const ENDPOINT = 'https://api.groq.com/openai/v1/chat/completions';

/* Ochiq LLM proksisi bo'lib qolmasin: kalit bepul kvotali, endpoint manzili esa
   ochiq kodda. Himoyasiz qolsa begonalar kvotani yoqib yuboradi va haqiqiy
   foydalanuvchilarda AI Coach o'ladi. */
const ALLOW_ORIGINS = [
  'https://focus-ai-final.vercel.app',
  'https://localhost',            // Capacitor (Android)
  'capacitor://localhost',        // Capacitor (iOS)
  'http://localhost:3000'
];
const RATE_MAX = 8;               // bir IP uchun daqiqasiga
const RATE_WIN = 60 * 1000;

module.exports = async (req, res) => {
  const origin = req.headers.origin || '';
  res.setHeader('Access-Control-Allow-Origin', ALLOW_ORIGINS.indexOf(origin) !== -1 ? origin : ALLOW_ORIGINS[0]);
  res.setHeader('Vary', 'Origin');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') { res.status(204).end(); return; }
  if (req.method !== 'POST') { res.status(405).json({ error: 'POST kerak' }); return; }

  /* oddiy IP-throttling (Vercel instansiyasi doirasida) */
  try {
    const ip = String(req.headers['x-forwarded-for'] || '').split(',')[0].trim() || 'x';
    const now = Date.now();
    global._coachRl = global._coachRl || {};
    const b = global._coachRl[ip] || { n: 0, t: now };
    if (now - b.t > RATE_WIN) { b.n = 0; b.t = now; }
    b.n++; global._coachRl[ip] = b;
    if (Object.keys(global._coachRl).length > 5000) global._coachRl = {};   // xotira o'smasin
    if (b.n > RATE_MAX) { res.status(429).json({ text: null, reason: 'rate' }); return; }
  } catch (e) { /* throttling ishlamasa ham so'rov o'tsin */ }

  const key = process.env.GROQ_KEY;
  if (!key) { res.status(200).json({ text: null, reason: 'no_key' }); return; }

  let body = req.body;
  if (typeof body === 'string') { try { body = JSON.parse(body); } catch (e) { body = {}; } }
  const message = (body && body.message ? String(body.message) : '').slice(0, 1000);
  const context = (body && body.context ? String(body.context) : '').slice(0, 2000);
  if (!message) { res.status(400).json({ error: 'message bosh' }); return; }

  const messages = [
    { role: 'system', content: 'Sen Focus AI ilovasidagi shaxsiy mentor-murabbiysan. Qisqa, samimiy va amaliy javob ber. Foydalanuvchi tilida javob ber (o\'zbek/rus/ingliz). ' + context },
    { role: 'user', content: message }
  ];

  let lastErr = null;
  for (let i = 0; i < MODELS.length; i++) {
    try {
      const r = await fetch(ENDPOINT, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + key },
        body: JSON.stringify({ model: MODELS[i], messages: messages, temperature: 0.8, max_tokens: 400 })
      });
      const j = await r.json();
      let text = null;
      try { text = j.choices[0].message.content; } catch (e) { text = null; }
      if (text) { res.status(200).json({ text: text, reason: 'ok', model: MODELS[i] }); return; }
      lastErr = { httpStatus: r.status, model: MODELS[i], error: j && j.error ? (j.error.message || j.error.type) : null };
    } catch (e) {
      lastErr = { model: MODELS[i], error: String(e) };
    }
  }
  res.status(200).json({ text: null, reason: 'empty', debug: lastErr });
};
