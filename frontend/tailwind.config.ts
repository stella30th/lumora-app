import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: "class",
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        lumora: {
          bg: "var(--lumora-bg)",
          surface: "var(--lumora-surface)",
          "surface-hover": "var(--lumora-surface-hover)",
          border: "var(--lumora-border)",
          primary: "var(--lumora-primary)",
          secondary: "var(--lumora-secondary)",
          muted: "var(--lumora-muted)",
          btn: "var(--lumora-btn-bg)",
          "btn-text": "var(--lumora-btn-text)",
          danger: "var(--lumora-danger)",
        },
      },
      fontFamily: {
        sans: ["var(--font-manrope)", "sans-serif"],
        mono: ["var(--font-ibm-mono)", "monospace"],
      },
      fontSize: {
        "title-lg": ["25px", { lineHeight: "1.2", fontWeight: "700" }],
        "title-page": ["22px", { lineHeight: "1.3", fontWeight: "700" }],
        "card-heading": ["14.5px", { lineHeight: "1.4", fontWeight: "600" }],
        "body-default": ["13px", { lineHeight: "1.5", fontWeight: "400" }],
        "caption-xs": ["11px", { lineHeight: "1.4", fontWeight: "400" }],
      },
      borderRadius: {
        card: "12px",
        btn: "8px",
        input: "8px",
        badge: "5px",
      },
    },
  },
  plugins: [],
};

export default config;
