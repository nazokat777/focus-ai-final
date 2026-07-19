#!/usr/bin/env bash
# Focus AI — bir martalik sozlash
# Ishlatish: terminalda shu papkaga kirib →  bash SETUP.sh

set -e
echo ""
echo "════════════════════════════════════════"
echo "  FOCUS AI — sozlash boshlandi"
echo "════════════════════════════════════════"
echo ""

# ── 1. Node bormi?
if ! command -v node >/dev/null 2>&1; then
  echo "❌ Node.js yo'q. Avval o'rnat: https://nodejs.org (LTS versiya)"
  echo "   O'rnatgach terminalni yopib qaytadan och va shu skriptni qayta ishga tushir."
  exit 1
fi
echo "✅ Node.js topildi: $(node --version)"

# ── 2. Claude Code
if ! command -v claude >/dev/null 2>&1; then
  echo ""
  echo "📦 Claude Code o'rnatilyapti..."
  curl -fsSL https://claude.ai/install.sh | bash
  export PATH="$HOME/.local/bin:$PATH"
  echo "✅ Claude Code o'rnatildi"
else
  echo "✅ Claude Code allaqachon bor"
fi

# ── 3. Skillar
echo ""
echo "🎨 Skillar o'rnatilyapti..."

echo "   → taste-skill (anti-slop dizayn didi)"
npx -y skills add https://github.com/Leonxlnx/taste-skill --skill "design-taste-frontend" || echo "   ⚠️ taste-skill o'tkazib yuborildi"

echo "   → impeccable (dizayn tili, 23 buyruq)"
npx -y impeccable install || echo "   ⚠️ impeccable o'tkazib yuborildi"

echo "   → gstack (Garry Tan to'plami)"
if [ ! -d "$HOME/.claude/skills/gstack" ]; then
  git clone -q https://github.com/garrytan/gstack.git "$HOME/.claude/skills/gstack" \
    && (cd "$HOME/.claude/skills/gstack" && ./setup) || echo "   ⚠️ gstack o'tkazib yuborildi"
else
  echo "     (allaqachon bor)"
fi

echo "   → Playwright MCP (brauzer/test)"
claude mcp add playwright -- npx @playwright/mcp@latest 2>/dev/null || echo "   ⚠️ Playwright o'tkazib yuborildi"

# ── 4. Loyiha paketlari
echo ""
echo "📱 Loyiha paketlari..."
npm install

echo ""
echo "════════════════════════════════════════"
echo "  ✅ TAYYOR!"
echo "════════════════════════════════════════"
echo ""
echo "  Endi shu buyruqni yoz:   claude"
echo ""
echo "  Claude Code ichida (har birini ALOHIDA yozasan):"
echo "    /plugin install superpowers@claude-plugins-official"
echo "    /plugin marketplace add https://github.com/zarazhangrui/frontend-slides"
echo "    /plugin install frontend-slides@frontend-slides"
echo "    /impeccable init"
echo ""
echo "  Keyin PROMPT.md faylini ochib, ichidagi matnni Claude Code'ga tashla."
echo ""
