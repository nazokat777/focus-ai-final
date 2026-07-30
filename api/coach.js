/* AI Coach — xavfsiz ko'prik (Vercel Serverless Function), Groq (bepul).
   Kalit FAQAT serverda (process.env.GROQ_KEY) — brauzerga hech qachon tushmaydi.
   Ilova POST /api/coach {message, context} yuboradi, javob {text} qaytadi. */

/* Groq bepul modellari — biri ishlamasa (429/404) keyingisiga o'tadi */
const MODELS = ['llama-3.3-70b-versatile', 'llama-3.1-8b-instant', 'gemma2-9b-it'];
const ENDPOINT = 'https://api.groq.com/openai/v1/chat/completions';

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') { res.status(204).end(); return; }
  if (req.method !== 'POST') { res.status(405).json({ error: 'POST kerak' }); return; }

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
