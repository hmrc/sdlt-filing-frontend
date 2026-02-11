document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("printButton");
  if (!btn) return;

  btn.addEventListener("click", (e) => {
    e.preventDefault();
    window.print();
  });
});
