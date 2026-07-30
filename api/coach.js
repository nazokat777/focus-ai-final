/* AI Coach — xavfsiz ko'prik (Vercel Serverless Function).
   Gemini kaliti FAQAT serverda (process.env.GEMINI_KEY) — brauzerga hech qachon tushmaydi.
   Ilova (web va native APK) shu manzilga so'rov yuboradi: POST /api/coach {message, context} */

const MODEL = 'gemini-2.0-flash';

module.exports = async (req, res) => {
  /* Native ilova boshqa origin'dan (https://localhost) chaqiradi — CORS kerak */
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') { res.status(204).end(); return; }
  if (req.method !== 'POST') { res.status(405).json({ error: 'POST kerak' }); return; }

  const key = process.env.GEMINI_KEY;
  if (!key) { res.status(200).json({ text: null, reason: 'no_key' }); return; }

  let body = req.body;
  if (typeof body === 'string') { try { body = JSON.parse(body); } catch (e) { body = {}; } }
  const message = (body && body.message ? String(body.message) : '').slice(0, 1000);
  const context = (body && body.context ? String(body.context) : '').slice(0, 2000);
  if (!message) { res.status(400).json({ error: 'message bo\'sh' }); return; }

  try {
    const r = await fetch(
      'https://generativelanguage.googleapis.com/v1beta/models/' + MODEL + ':generateContent?key=' + encodeURIComponent(key),
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ role: 'user', parts: [{ text: context + '\n\nSavol: ' + message }] }],
          generationConfig: { temperature: 0.8, maxOutputTokens: 400 }
        })
      }
    );
    const j = await r.json();
    let text = null;
    try { text = j.candidates[0].content.parts[0].text; } catch (e) { text = null; }
    res.status(200).json({ text: text, reason: text ? 'ok' : 'empty' });
  } catch (e) {
    res.status(200).json({ text: null, reason: 'error' });
  }
};
