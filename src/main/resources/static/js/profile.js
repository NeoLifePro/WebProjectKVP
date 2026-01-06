document.addEventListener("DOMContentLoaded", function() {
    // ===== Ielādē profila datus  =====
    fetch('/api/profile/details')
        .then(response => response.json())
        .then(data => {
            document.getElementById('username').textContent = data.username;
            document.getElementById('email').textContent = data.email;
        })
        .catch(error => {
            console.error('Error fetching user details:', error);
        });

    // ===== Notikums: paroles atjaunošanas poga =====
    const updatePasswordButton = document.getElementById('updatePasswordButton');
    updatePasswordButton.addEventListener('click', function() {
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Validācija: jaunajām parolēm jāsakrīt
        if (newPassword !== confirmPassword) {
            displayMessage('error', 'New passwords do not match!');
            return;
        }

        // Payload parolei mainīt
        const payload = {
            currentPassword: currentPassword,
            newPassword: newPassword
        };

        // ===== Nosūta pieprasījumu backendam paroles maiņai =====
        fetch('/api/profile/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        })
            .then(response => response.text())
            .then(message => {
                if (message.includes('success')) {
                    displayMessage('success', 'Password updated successfully!');
                } else {
                    displayMessage('error', message);
                }
            })
            .catch(error => {
                console.error('Error updating password:', error);
                displayMessage('error', 'An error occurred. Please try again later.');
            });
    });

    // ===== Palīgfunkcija: parāda kļūdas vai veiksmes ziņu UI =====
    function displayMessage(type, message) {
        const errorMessage = document.getElementById('error-message');
        const successMessage = document.getElementById('success-message');

        // Kļūdas gadījumā — rādam error bloku, paslēpjam success
        if (type === 'error') {
            errorMessage.textContent = message;
            errorMessage.style.display = 'block';
            successMessage.style.display = 'none';
        }
        // Veiksmes gadījumā — rādam success bloku, paslēpjam error
        else if (type === 'success') {
            successMessage.textContent = message;
            successMessage.style.display = 'block';
            errorMessage.style.display = 'none';
        }
    }
});
