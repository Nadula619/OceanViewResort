function initDashboard(role) {
    console.log('OceanView Dashboard Initializing (Role: ' + role + ')');

    const state = {
        role: role,
        currentPage: 'overview',
        rooms: [],
        bookings: [],
        guests: [],
        staff: [],
        payments: []
    };

    // --- Helper Functions ---
    function getStatusBadgeClass(status) {
        if (['AVAILABLE', 'CONFIRMED', 'CHECKED_IN'].includes(status)) return 'badge-success';
        if (['OCCUPIED', 'PENDING'].includes(status)) return 'badge-warning';
        if (status === 'COMPLETED' || status === 'CANCELLED') return 'badge-danger';
        return 'badge-danger';
    }

    function updateOverview() {
        try {
            if (role === 'ADMIN') {
                const elTotal = document.getElementById('statTotalRooms');
                const elActive = document.getElementById('statActiveBookings');
                const elRev = document.getElementById('statRevenue');

                if (elTotal) elTotal.innerText = state.rooms.length;
                if (elActive) elActive.innerText = state.bookings.filter(b => b.status === 'CONFIRMED' || b.status === 'CHECKED_IN').length;
                if (elRev) {
                    const revenue = state.bookings.reduce((sum, b) => sum + (b.totalPrice || 0), 0);
                    elRev.innerText = 'LKR ' + revenue.toLocaleString(undefined, { minimumFractionDigits: 2 });
                }
            } else {
                const elAvail = document.getElementById('statAvailable');
                const elArr = document.getElementById('statArrivals');
                const elDep = document.getElementById('statDepartures');

                if (elAvail) elAvail.innerText = state.rooms.filter(r => r.status === 'AVAILABLE').length;
                if (elArr) elArr.innerText = state.bookings.filter(b => b.status === 'CONFIRMED').length;
                if (elDep) elDep.innerText = state.bookings.filter(b => b.status === 'CHECKED_IN').length;
            }
        } catch (e) {
            console.warn('Overview update failed (likely missing elements on this page):', e.message);
        }
    }

    // --- Data Loading ---
    window.loadRooms = function () {
        fetch('api/rooms')
            .then(res => res.json())
            .then(data => {
                state.rooms = data;
                renderRoomsTable();
                updateOverview();
            }).catch(err => console.error('Load Rooms failed:', err));
    };

    function loadBookings() {
        console.log('Loading bookings...');
        fetch('api/bookings')
            .then(res => res.json())
            .then(data => {
                state.bookings = data;
                if (state.currentPage === 'bookings') renderBookingsTable();
                updateOverview();
            }).catch(err => console.error('Load Bookings failed:', err));
    }

    function loadPayments(search = '') {
        console.log('Loading payments...', search);
        fetch(`api/payments${search ? '?search=' + encodeURIComponent(search) : ''}`)
            .then(res => res.json())
            .then(data => {
                state.payments = data;
                if (state.currentPage === 'payments') renderPaymentsTable();
            }).catch(err => console.error('Load Payments failed:', err));
    }

    window.loadStaff = function () {
        fetch('api/staff')
            .then(res => res.json())
            .then(data => {
                state.staff = data;
                renderStaffTable();
            }).catch(err => console.error('Load Staff failed:', err));
    };

    // --- Rendering ---
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
                    <div style="font-weight: 500;">${booking.firstName} ${booking.lastName}</div>
                    <div style="font-size: 12px; color: var(--text-muted);">${booking.email}</div>
                </td>
                <td>Room ${booking.roomNumber || booking.roomId}</td>
                <td>
                    <div style="font-size: 13px;">In: ${booking.checkInDate}</div>
                    <div style="font-size: 13px;">Out: ${booking.checkOutDate}</div>
                </td>
                <td><span class="badge ${getStatusBadgeClass(booking.status)}">${booking.status}</span></td>
                <td>
                    ${role === 'RECEPTIONIST' ? `
                        ${booking.status === 'CONFIRMED' ? `
                            <button class="btn btn-primary" onclick="updateBookingStatus(${booking.id}, 'CHECKED_IN')" style="padding: 5px 10px;">Check In</button>
                        ` : ''}
                        ${booking.status === 'CHECKED_IN' ? `
                            <button class="btn" onclick="updateBookingStatus(${booking.id}, 'COMPLETED')" style="background: var(--glass); padding: 5px 10px;">Check Out</button>
                        ` : ''}
                    ` : `LKR ${booking.totalPrice}`}
                </td>
            </tr>
        `).join('');
    }

    function renderPaymentsTable() {
        const tbody = document.querySelector('#paymentsTable tbody');
        if (!tbody) return;

        if (state.payments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 40px; color: var(--text-muted);">No payment records found.</td></tr>';
            return;
        }

        tbody.innerHTML = state.payments.map(p => `
            <tr>
                <td style="font-family: monospace; font-weight: 600; color: var(--primary);">${p.transactionId || 'N/A'}</td>
                <td>
                    <div style="font-weight: 500;">${p.guestName}</div>
                    <div style="font-size: 11px; color: var(--text-muted);">Room ${p.roomNumber}</div>
                </td>
                <td style="font-weight: 600;">LKR ${p.amount.toLocaleString()}</td>
                <td><span class="badge" style="background: rgba(255,255,255,0.05);">${p.paymentMethod}</span></td>
                ${state.role !== 'ADMIN' ? `
                <td>
                    <button class="btn" onclick="generateBill(${p.id})" style="padding: 5px 12px; background: var(--primary); font-size: 12px;">Generate Bill</button>
                </td>` : ''}
            </tr>
        `).join('');
    }

    window.generateBill = function (paymentId) {
        console.log('Generating bill for:', paymentId);
        const payment = state.payments.find(p => p.id === paymentId);
        if (!payment) return;

        // Populate Template
        document.getElementById('billDate').innerText = new Date(payment.paymentDate).toLocaleDateString();
        document.getElementById('billGuestName').innerText = payment.guestName;
        document.getElementById('billMethod').innerText = payment.paymentMethod;
        document.getElementById('billTxID').innerText = payment.transactionId || 'CASH-REC-' + payment.id;
        document.getElementById('billRoom').innerText = payment.roomNumber;
        document.getElementById('billAmount').innerText = 'LKR ' + payment.amount.toLocaleString();
        document.getElementById('billTotal').innerText = 'LKR ' + payment.amount.toLocaleString();

        // Print Logic
        const printContent = document.getElementById('billTemplate').innerHTML;
        const originalContent = document.body.innerHTML;

        const printWindow = window.open('', '_blank', 'height=600,width=800');
        printWindow.document.write('<html><head><title>Payment Bill - OceanView</title>');
        printWindow.document.write('<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">');
        printWindow.document.write('</head><body>');
        printWindow.document.write(printContent);
        printWindow.document.write('</body></html>');
        printWindow.document.close();

        setTimeout(() => {
            printWindow.print();
            printWindow.close();
        }, 500);
    };

    function renderStaffTable() {
        const tbody = document.querySelector('#staffTable tbody');
        if (!tbody) return;
        tbody.innerHTML = state.staff.map(staff => `
            <tr>
                <td>${staff.firstName} ${staff.lastName}</td>
                <td>${staff.email}</td>
                <td><span class="badge ${staff.role === 'ADMIN' ? 'badge-danger' : 'badge-success'}">${staff.role}</span></td>
                <td>${staff.username}</td>
                <td>
                    <button class="btn" onclick="editStaff(${staff.staffId})" style="background: var(--glass); padding: 5px 10px;">Edit</button>
                    <button class="btn" onclick="deleteStaff(${staff.staffId})" style="background: rgba(239, 68, 68, 0.1); color: var(--danger); padding: 5px 10px;">Delete</button>
                </td>
            </tr>
        `).join('');
    }

    // --- Global Handlers ---
    window.switchPage = function (pageId) {
        state.currentPage = pageId;
        const navItems = document.querySelectorAll('.nav-item');
        const pages = document.querySelectorAll('.page');
        const pageTitle = document.getElementById('pageTitle');

        navItems.forEach(item => {
            item.classList.toggle('active', item.getAttribute('data-page') === pageId);
        });

        pages.forEach(page => {
            page.style.display = page.id === pageId + 'Page' ? 'block' : 'none';
        });

        const activeNav = document.querySelector(`.nav-item[data-page="${pageId}"]`);
        if (activeNav && pageTitle) pageTitle.innerText = activeNav.innerText;

        if (pageId === 'rooms' || pageId === 'availability') loadRooms();
        if (pageId === 'bookings') loadBookings();
        if (pageId === 'staff') loadStaff();
        if (pageId === 'payments') loadPayments(); // Added this line
        if (pageId === 'overview') updateOverview();
    };

    // Payment Search listener
    const paySearch = document.getElementById('paymentSearchInput');
    if (paySearch) {
        paySearch.addEventListener('input', (e) => {
            loadPayments(e.target.value);
        });
    }

    window.showAddStaffModal = function () {
        const modal = document.getElementById('staffModal');
        if (!modal) return;
        const form = document.getElementById('staffForm');
        if (form) form.reset();
        const title = document.getElementById('staffModalTitle');
        if (title) title.innerText = 'Add New Staff Member';
        const idInput = modal.querySelector('input[name="id"]');
        if (idInput) idInput.value = '';
        modal.style.display = 'flex';
    };

    window.closeStaffModal = function () {
        const modal = document.getElementById('staffModal');
        if (modal) modal.style.display = 'none';
    };

    window.editStaff = function (id) {
        const staff = state.staff.find(s => s.staffId === id);
        if (!staff) return;

        window.showAddStaffModal();
        const modal = document.getElementById('staffModal');
        const title = document.getElementById('staffModalTitle');
        if (title) title.innerText = 'Edit Staff: ' + staff.firstName;

        const form = document.getElementById('staffForm');
        if (form) {
            form.querySelector('input[name="id"]').value = staff.staffId;
            form.querySelector('input[name="firstName"]').value = staff.firstName;
            form.querySelector('input[name="lastName"]').value = staff.lastName;
            form.querySelector('input[name="email"]').value = staff.email;
            form.querySelector('input[name="phone"]').value = staff.phone || '';
            form.querySelector('select[name="role"]').value = staff.role;
            form.querySelector('input[name="username"]').value = staff.username;
            form.querySelector('input[name="password"]').value = '';
        }
    };

    window.deleteStaff = function (id) {
        if (!confirm('Are you sure you want to remove this staff member?')) return;
        const params = new URLSearchParams();
        params.append('action', 'delete');
        params.append('id', id);

        fetch('api/staff', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }).then(() => loadStaff());
    };

    window.showAddRoomModal = function () {
        const modal = document.getElementById('roomModal');
        if (!modal) return;
        const form = document.getElementById('roomForm');
        if (form) form.reset();
        const title = document.getElementById('modalTitle');
        if (title) title.innerText = 'Add New Room';
        const idInput = modal.querySelector('input[name="id"]');
        if (idInput) idInput.value = '';
        modal.style.display = 'flex';
    };

    window.closeRoomModal = function () {
        const modal = document.getElementById('roomModal');
        if (modal) modal.style.display = 'none';
    };

    window.editRoom = function (id) {
        const room = state.rooms.find(r => r.id === id);
        if (!room) return;

        window.showAddRoomModal();
        const title = document.getElementById('modalTitle');
        if (title) title.innerText = 'Edit Room ' + room.roomNumber;

        const form = document.getElementById('roomForm');
        if (form) {
            form.querySelector('input[name="id"]').value = room.id;
            form.querySelector('input[name="roomNumber"]').value = room.roomNumber;
            form.querySelector('select[name="roomType"]').value = room.roomType;
            form.querySelector('input[name="price"]').value = room.pricePerNight;
            form.querySelector('select[name="status"]').value = room.status;
            form.querySelector('textarea[name="description"]').value = room.description || '';
        }
    };

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
    };

    window.showAddBookingModal = function () {
        const modal = document.getElementById('bookingModal');
        const form = document.getElementById('bookingForm');
        const guestSelect = document.getElementById('guestSelect');
        const guestCard = document.getElementById('guestDetailsCard');
        const statusBadge = document.getElementById('guestStatusBadge');
        const newIndicator = document.getElementById('newGuestIndicator');
        const roomSelect = document.getElementById('roomSelect');

        if (!modal || !form) return;
        form.reset();
        if (guestCard) guestCard.style.display = 'none';
        if (statusBadge) statusBadge.style.display = 'none';

        // Load Guests
        fetch('api/users')
            .then(res => res.json())
            .then(users => {
                state.guests = users;
                if (guestSelect) {
                    guestSelect.innerHTML = '<option value="">-- Choose an existing guest --</option>' +
                        '<option value="NEW" style="font-weight: 700; color: var(--primary);">+ REGISTER NEW GUEST</option>' +
                        users.map(u => `<option value="${u.id}">${u.firstName} ${u.lastName} (${u.email})</option>`).join('');
                }
            });

        // Load Rooms
        if (roomSelect) {
            roomSelect.innerHTML = '<option value="">Loading available rooms...</option>';
            fetch('api/rooms')
                .then(res => res.json())
                .then(rooms => {
                    const availableRooms = rooms.filter(r => r.status === 'AVAILABLE');
                    if (availableRooms.length === 0) {
                        roomSelect.innerHTML = '<option value="">No rooms available</option>';
                    } else {
                        roomSelect.innerHTML = '<option value="">Select Room...</option>' +
                            availableRooms.map(r => `<option value="${r.id}">${r.roomNumber} - ${r.roomType} (LKR ${r.pricePerNight})</option>`).join('');
                    }
                })
                .catch(err => {
                    console.error('Failed to load rooms for booking:', err);
                    roomSelect.innerHTML = '<option value="">Error loading rooms</option>';
                });
        }

        // Search Functionality
        const guestSearchInput = document.getElementById('guestSearchInput');
        function filterGuests(query) {
            if (!guestSelect) return;
            const q = query.toLowerCase();
            const filtered = state.guests.filter(g =>
                g.firstName.toLowerCase().includes(q) ||
                g.lastName.toLowerCase().includes(q) ||
                g.email.toLowerCase().includes(q)
            );

            let html = '<option value="">-- ' + (q ? 'Found ' + filtered.length + ' guests' : 'Search or select below') + ' --</option>';
            html += '<option value="NEW" style="font-weight: 700; color: var(--primary);">✨ NOT LISTED? REGISTER NEW GUEST</option>';
            html += filtered.map(u => `<option value="${u.id}">${u.firstName} ${u.lastName} (${u.email})</option>`).join('');
            guestSelect.innerHTML = html;
        }

        if (guestSearchInput) {
            guestSearchInput.oninput = (e) => filterGuests(e.target.value);
        }

        if (guestSelect) {
            guestSelect.onchange = function () {
                const val = this.value;
                const isNew = val === 'NEW';
                const hasGuest = val !== '';

                if (guestCard) guestCard.style.display = hasGuest ? 'block' : 'none';
                if (statusBadge) statusBadge.style.display = (hasGuest && !isNew) ? 'block' : 'none';
                if (newIndicator) newIndicator.style.display = isNew ? 'block' : 'none';

                // Clear search if selecting "NEW"
                if (isNew && guestSearchInput) guestSearchInput.value = '';

                const inputs = guestCard ? guestCard.querySelectorAll('input, textarea') : [];
                inputs.forEach(f => {
                    f.required = hasGuest;
                    f.readOnly = !isNew;
                    f.value = '';
                });

                if (!isNew && val) {
                    const guest = state.guests.find(g => g.id == val);
                    if (guest) {
                        form.querySelector('input[name="first_name"]').value = guest.firstName;
                        form.querySelector('input[name="last_name"]').value = guest.lastName;
                        form.querySelector('input[name="email"]').value = guest.email;
                        form.querySelector('input[name="phone"]').value = guest.phone;
                        form.querySelector('textarea[name="address"]').value = guest.address;
                    }
                }
            };
        }

        const calculatePrice = () => {
            const roomId = roomSelect ? roomSelect.value : null;
            const checkIn = document.getElementById('checkInDate').value;
            const checkOut = document.getElementById('checkOutDate').value;
            if (roomId && checkIn && checkOut) {
                const room = state.rooms.find(r => r.id == roomId);
                const start = new Date(checkIn);
                const end = new Date(checkOut);
                const diff = end - start;
                const nights = Math.ceil(diff / (1000 * 60 * 60 * 24));
                const display = document.getElementById('displayTotalPrice');
                const input = document.getElementById('totalPriceInput');
                if (nights > 0 && room) {
                    const total = nights * room.pricePerNight;
                    if (display) display.innerText = 'LKR ' + total.toLocaleString(undefined, { minimumFractionDigits: 2 });
                    if (input) input.value = total.toFixed(2);
                } else {
                    if (display) display.innerText = 'LKR 0.00';
                    if (input) input.value = '';
                }
            }
        };

        if (roomSelect) roomSelect.onchange = calculatePrice;
        const ci = document.getElementById('checkInDate');
        const co = document.getElementById('checkOutDate');
        if (ci) ci.onchange = calculatePrice;
        if (co) co.onchange = calculatePrice;

        form.onsubmit = function (e) {
            e.preventDefault();
            const formData = new FormData(this);
            const params = new URLSearchParams();
            params.append('action', 'add');
            for (let pair of formData.entries()) {
                if (pair[0] !== 'guestId') params.append(pair[0], pair[1]);
            }

            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerText = 'Processing...';
            }

            fetch('api/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            }).then(res => res.json()).then(data => {
                if (data.success) {
                    alert('✅ Booking confirmed successfully!');
                    window.closeBookingModal();
                    loadBookings();
                    loadRooms();
                } else {
                    alert('❌ Booking failed: ' + (data.message || 'Please check inputs and room availability.'));
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.innerText = 'Confirm & Book';
                    }
                }
            }).catch(err => {
                alert('An error occurred during booking.');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerText = 'Confirm & Book';
                }
            });
        };

        modal.style.display = 'flex';
    };

    window.closeBookingModal = function () {
        const modal = document.getElementById('bookingModal');
        if (modal) modal.style.display = 'none';
    };

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
    };

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
    };

    // --- Events & Init ---
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const pageId = item.getAttribute('data-page');
            window.switchPage(pageId);
        });
    });

    const staffForm = document.getElementById('staffForm');
    if (staffForm) {
        staffForm.onsubmit = function (e) {
            e.preventDefault();
            const formData = new FormData(this);
            const id = formData.get('id');
            const action = id ? 'update' : 'add';
            const params = new URLSearchParams();
            params.append('action', action);
            for (let pair of formData.entries()) params.append(pair[0], pair[1]);

            fetch('api/staff', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            }).then(res => res.json()).then(data => {
                if (data.success) {
                    window.closeStaffModal();
                    loadStaff();
                } else {
                    alert('Operation failed. Please check inputs.');
                }
            });
        };
    }

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
            }).then(res => res.json()).then(data => {
                if (data.success) {
                    window.closeRoomModal();
                    loadRooms();
                }
            });
        };
    }

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.onclick = () => {
            fetch('api/auth?action=logout', { method: 'POST' })
                .then(() => { window.location.replace('login.html'); })
                .catch(() => { window.location.replace('login.html'); });
        };
    }

    // Initial Data Load
    loadRooms();
    loadBookings();

    // Set Welcome Name & Auth Check
    fetch('api/auth')
        .then(res => res.json())
        .then(data => {
            if (!data.loggedIn || (data.role && data.role.toUpperCase() !== role.toUpperCase())) {
                window.location.replace('login.html');
            } else {
                const userName = document.getElementById('userName');
                if (userName) userName.innerText = data.name;
                updateOverview();
            }
        }).catch(() => window.location.replace('login.html'));
}


