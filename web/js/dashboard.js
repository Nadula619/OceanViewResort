function initDashboard(role) {
    const state = {
        role: role,
        currentPage: 'overview',
        rooms: [],
        bookings: []
    };

    // UI Elements
    const navItems = document.querySelectorAll('.nav-item');
    const pages = document.querySelectorAll('.page');
    const pageTitle = document.getElementById('pageTitle');
    const userName = document.getElementById('userName');

    // Navigation
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const pageId = item.getAttribute('data-page');
            switchPage(pageId);
        });
    });

    function switchPage(pageId) {
        state.currentPage = pageId;

        // Update Nav
        navItems.forEach(item => {
            item.classList.toggle('active', item.getAttribute('data-page') === pageId);
        });

        // Update Visibility
        pages.forEach(page => {
            page.style.display = page.id === pageId + 'Page' ? 'block' : 'none';
        });

        // Update Title
        const activeNav = document.querySelector(`.nav-item[data-page="${pageId}"]`);
        pageTitle.innerText = activeNav.innerText;

        // Load Data
        if (pageId === 'rooms' || pageId === 'availability') loadRooms();
        if (pageId === 'bookings') loadBookings();
        if (pageId === 'overview') updateOverview();
    }

    // Auth check
    fetch('api/auth')
        .then(res => res.json())
        .then(data => {
            if (!data.loggedIn || data.role !== role) {
                window.location.replace('login.html');
            } else {
                userName.innerText = data.name;
                updateOverview();
            }
        })
        .catch(() => {
            window.location.replace('login.html');
        });

    // Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.onclick = () => {
            fetch('api/auth?action=logout', { method: 'POST' })
                .then(() => {
                    window.location.replace('login.html');
                })
                .catch(err => {
                    console.error('Logout failed:', err);
                    window.location.replace('login.html'); // Redirect anyway
                });
        };
    }

    // Data Loading
    window.loadRooms = function () {
        fetch('api/rooms')
            .then(res => res.json())
            .then(data => {
                state.rooms = data;
                renderRoomsTable();
                updateOverview();
            });
    }

    window.loadBookings = function () {
        fetch('api/bookings')
            .then(res => res.json())
            .then(data => {
                state.bookings = data;
                renderBookingsTable();
                updateOverview();
            });
    }

    function renderRoomsTable() {
        const tbody = document.querySelector('#roomsTable tbody');
        if (!tbody) return;
        tbody.innerHTML = state.rooms.map(room => `
            <tr>
                <td>${room.roomNumber}</td>
                <td>${room.roomType}</td>
                <td>LKR ${room.pricePerNight}</td>
                <td><span class="badge ${getStatusBadgeClass(room.status)}">${room.status}</span></td>
                <td>
                    ${role === 'ADMIN' ? `
                        <button class="btn" onclick="editRoom(${room.id})" style="background: var(--glass); padding: 5px 10px;">Edit</button>
                        <button class="btn" onclick="deleteRoom(${room.id})" style="background: rgba(239, 68, 68, 0.1); color: var(--danger); padding: 5px 10px;">Delete</button>
                    ` : `
                        <select onchange="updateRoomStatus(${room.id}, this.value)" class="input-field" style="margin: 0; padding: 5px; width: auto;">
                            <option value="AVAILABLE" ${room.status === 'AVAILABLE' ? 'selected' : ''}>Available</option>
                            <option value="OCCUPIED" ${room.status === 'OCCUPIED' ? 'selected' : ''}>Occupied</option>
                            <option value="MAINTENANCE" ${room.status === 'MAINTENANCE' ? 'selected' : ''}>Maintenance</option>
                        </select>
                    `}
                </td>
            </tr>
        `).join('');
    }

    function renderBookingsTable() {
        const tbody = document.querySelector('#bookingsTable tbody');
        if (!tbody) return;
        tbody.innerHTML = state.bookings.map(booking => `
            <tr>
                <td>
                    <div style="font-weight: 500;">${booking.customerName}</div>
                    <div style="font-size: 12px; color: var(--text-muted);">${booking.customerEmail}</div>
                </td>
                <td>Room ${booking.roomId}</td>
                <td>
                    <div style="font-size: 13px;">In: ${booking.checkInDate}</div>
                    <div style="font-size: 13px;">Out: ${booking.checkOutDate}</div>
                </td>
                <td><span class="badge ${getStatusBadgeClass(booking.status)}">${booking.status}</span></td>
                <td>
                    ${role === 'RECEPTIONIST' ? `
                        <button class="btn btn-primary" onclick="updateBookingStatus(${booking.id}, 'CHECKED_IN')" style="padding: 5px 10px;">Check In</button>
                    ` : `LKR ${booking.totalPrice}`}
                </td>
            </tr>
        `).join('');
    }

    function updateOverview() {
        if (role === 'ADMIN') {
            document.getElementById('statTotalRooms').innerText = state.rooms.length;
            document.getElementById('statActiveBookings').innerText = state.bookings.filter(b => b.status === 'CONFIRMED' || b.status === 'CHECKED_IN').length;
            const revenue = state.bookings.reduce((sum, b) => sum + b.totalPrice, 0);
            document.getElementById('statRevenue').innerText = 'LKR ' + revenue.toFixed(2);
        } else {
            document.getElementById('statAvailable').innerText = state.rooms.filter(r => r.status === 'AVAILABLE').length;
            document.getElementById('statArrivals').innerText = state.bookings.filter(b => b.status === 'CONFIRMED').length;
            document.getElementById('statDepartures').innerText = state.bookings.filter(b => b.status === 'CHECKED_IN').length;
        }
    }

    function getStatusBadgeClass(status) {
        if (['AVAILABLE', 'CONFIRMED', 'CHECKED_IN'].includes(status)) return 'badge-success';
        if (['OCCUPIED', 'PENDING'].includes(status)) return 'badge-warning';
        return 'badge-danger';
    }

    // Modal Helpers
    window.showAddRoomModal = function () {
        const modal = document.getElementById('roomModal');
        document.getElementById('roomForm').reset();
        document.getElementById('modalTitle').innerText = 'Add New Room';
        modal.querySelector('input[name="id"]').value = '';
        modal.style.display = 'flex';
    }

    window.closeRoomModal = function () {
        document.getElementById('roomModal').style.display = 'none';
    }

    // Room Form Submit
    const roomForm = document.getElementById('roomForm');
    if (roomForm) {
        roomForm.onsubmit = function (e) {
            e.preventDefault();
            const formData = new FormData(this);
            const id = formData.get('id');
            const action = id ? 'update' : 'add';

            const params = new URLSearchParams();
            params.append('action', action);
            for (let pair of formData.entries()) params.append(pair[0], pair[1]);

            fetch('api/rooms', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        closeRoomModal();
                        loadRooms();
                    }
                });
        };
    }

    // Booking Modal Helpers
    window.showAddBookingModal = function () {
        const modal = document.getElementById('bookingModal');
        document.getElementById('bookingForm').reset();

        // Populate available rooms
        const roomSelect = document.getElementById('roomSelect');
        roomSelect.innerHTML = '<option value="">Select Room</option>' +
            state.rooms.filter(r => r.status === 'AVAILABLE')
                .map(r => `<option value="${r.id}">${r.roomNumber} - ${r.roomType} (LKR ${r.pricePerNight})</option>`)
                .join('');

        modal.style.display = 'flex';
    }

    window.closeBookingModal = function () {
        document.getElementById('bookingModal').style.display = 'none';
    }

    const bookingForm = document.getElementById('bookingForm');
    if (bookingForm) {
        bookingForm.onsubmit = function (e) {
            e.preventDefault();
            const formData = new FormData(this);
            const params = new URLSearchParams();
            params.append('action', 'add');
            for (let pair of formData.entries()) params.append(pair[0], pair[1]);

            fetch('api/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        closeBookingModal();
                        loadBookings();
                        loadRooms(); // Refresh to show room as occupied if logic exists
                    }
                });
        };
    }

    // Initial Load
    loadRooms();
    loadBookings();
}

window.editRoom = function (id) {
    const room = state.rooms.find(r => r.id === id);
    if (!room) return;

    showAddRoomModal();
    const modal = document.getElementById('roomModal');
    document.getElementById('modalTitle').innerText = 'Edit Room ' + room.roomNumber;

    const form = document.getElementById('roomForm');
    form.querySelector('input[name="id"]').value = room.id;
    form.querySelector('input[name="roomNumber"]').value = room.roomNumber;
    form.querySelector('select[name="roomType"]').value = room.roomType;
    form.querySelector('input[name="price"]').value = room.pricePerNight;
    form.querySelector('select[name="status"]').value = room.status;
    form.querySelector('textarea[name="description"]').value = room.description || '';
}

window.updateRoomStatus = function (id, status) {
    const room = state.rooms.find(r => r.id === id);
    if (!room) return;

    const params = new URLSearchParams();
    params.append('action', 'update');
    params.append('id', id);
    params.append('roomNumber', room.roomNumber);
    params.append('roomType', room.roomType);
    params.append('price', room.pricePerNight);
    params.append('status', status);
    params.append('description', room.description || '');

    fetch('api/rooms', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    }).then(() => loadRooms());
}

window.deleteRoom = function (id) {
    if (!confirm('Are you sure you want to delete this room?')) return;
    const params = new URLSearchParams();
    params.append('action', 'delete');
    params.append('id', id);

    fetch('api/rooms', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    }).then(() => loadRooms());
}

window.updateBookingStatus = function (id, status) {
    const params = new URLSearchParams();
    params.append('action', 'updateStatus');
    params.append('id', id);
    params.append('status', status);

    fetch('api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    }).then(() => loadBookings());
}
