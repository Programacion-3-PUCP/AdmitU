(function () {
    var tema = "light";
    try {
        tema = localStorage.getItem("admitu-tema")
            || (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
    } catch (e) { }
    document.documentElement.setAttribute("data-theme", tema);

    window.admitu = {
        alternarTema: function () {
            var actual = document.documentElement.getAttribute("data-theme") === "dark" ? "light" : "dark";
            document.documentElement.setAttribute("data-theme", actual);
            try { localStorage.setItem("admitu-tema", actual); } catch (e) { }
        }
    };
})();
