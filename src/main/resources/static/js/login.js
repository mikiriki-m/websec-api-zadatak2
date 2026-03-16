if (getToken()) {
    window.location.href = "app.html";
}

async function login() {
    error.innerText = "";

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                email: email.value,
                password: password.value
            })
        });

        if (!response.ok) {
            if (response.status === 423) {
                error.innerText = "Too many failed attempts. Account is temporarily locked.";
            } else {
                error.innerText = "Invalid credentials";
            }
            return;
        }

        const data = await response.json();
        setToken(data.accessToken);
        window.location.href = "app.html";

    } catch (err) {
        error.innerText = "Connection error";
        console.error(err);
    }
}
