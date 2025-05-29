module.exports = {
  root: true,
  env: {
    node: true,
    es6: true,
  },
  extends: ["eslint:recommended", "google"],
  rules: {
    "quotes": ["error", "double"],
    "indent": ["error", 2],
    "object-curly-spacing": ["error", "always"],
    "max-len": ["error", { "code": 120 }]
  },
};