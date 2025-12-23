(() => {
  const { lat, lng, zoom } = window.MAP_CONFIG;

  /* ================= MAP INIT ================= */

  const map = L.map('map', { zoomControl: false })
    .setView([lat, lng], zoom);

  map.attributionControl.setPrefix(false);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  const markersLayer = L.layerGroup().addTo(map);

 const modal = document.getElementById('spotModal');
  const modalBackdrop = document.getElementById('modalBackdrop');
  const modalCloseBtn = document.getElementById('modalCloseBtn');

  const spotForm = document.getElementById('spotForm');
  const spotIdInput = document.getElementById('spotId');
  const descInput = document.getElementById('spotDescription');
  const latInput = document.getElementById('spotLat');
  const lngInput = document.getElementById('spotLng');
  const occupiedCheckbox = document.getElementById('spotOccupied');
  const modalError = document.getElementById('modalError');

  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelEditBtn = document.getElementById('cancelEditBtn');

  /* ================= HELPERS ================= */

  function colorByState(state) {
    switch (state) {
      case 'FREE': return 'green';
      case 'OCCUPIED': return 'red';
      case 'RESERVED': return 'orange';
      default: return 'blue';
    }
  }

  function clearMarkers() {
    markersLayer.clearLayers();
  }

function openModal(spot) {
    // spot: { id, description, cords:{lat,lng}, state, ownerId, canEdit }
    spotIdInput.value = spot.id;
    descInput.value = spot.description || '';
    latInput.value = spot.cords.lat;
    lngInput.value = spot.cords.lng;
    occupiedCheckbox.checked = (spot.state === 'OCCUPIED');
    modalError.style.display = 'none';
    modalError.textContent = '';

    // set readonly by default
    setEditable(false);

    // controls depending on permission
    if (spot.canEdit) {
      editBtn.style.display = 'inline-block';
      // editing buttons hidden until Edit clicked
      saveBtn.style.display = 'none';
      cancelEditBtn.style.display = 'none';
    } else {
      console.error('Нелоьзя!!!!!');
      editBtn.style.display = 'none';
      saveBtn.style.display = 'none';
      cancelEditBtn.style.display = 'none';
    }

    // checkbox ability: if user can change occupancy (use canEdit or separate flag)
    occupiedCheckbox.disabled = !spot.canEdit;

    modal.classList.add('active');
    modal.setAttribute('aria-hidden', 'false');
  }

  function closeModal() {
    modal.classList.remove('active');
    modal.setAttribute('aria-hidden', 'true');
  }

  function setEditable(on) {
    descInput.readOnly = !on;
    latInput.readOnly = !on;
    lngInput.readOnly = !on;

    if (on) {
      saveBtn.style.display = 'inline-block';
      cancelEditBtn.style.display = 'inline-block';
      editBtn.style.display = 'none';
    } else {
      saveBtn.style.display = 'none';
      cancelEditBtn.style.display = 'none';
      // editBtn visibility managed by openModal
    }
  }

  modalCloseBtn.addEventListener('click', closeModal);
  modalBackdrop.addEventListener('click', closeModal);

  editBtn.addEventListener('click', () => setEditable(true));
  cancelEditBtn.addEventListener('click', () => {
    // revert fields to original by reloading spot from server or closing modal
    const id = spotIdInput.value;
    fetch(`/api/parking/${id}`, { credentials: 'same-origin' })
      .then(r => r.ok ? r.json() : Promise.reject('Не удалось получить данные'))
      .then(spot => openModal(spot))
      .catch(err => {
        modalError.style.display = 'block';
        modalError.textContent = String(err);
      });
  });

  // Save (update spot data)
  spotForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = spotIdInput.value;
    const body = {
      description: descInput.value,
      cords: {
        lat: parseFloat(latInput.value),
        lng: parseFloat(lngInput.value)
      }
    };

    try {
      const res = await fetch(`/api/parking/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(body)
      });
      if (res.status === 403) {
        throw new Error('Нет прав на изменение');
      }
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || 'Ошибка сервера');
      }
      // обновляем маркеры (или только этот маркер)
      await loadSpots();
      closeModal();
    } catch (err) {
      modalError.style.display = 'block';
      modalError.textContent = err.message || String(err);
    }
  });

  // Toggle occupied via checkbox
  occupiedCheckbox.addEventListener('change', async () => {
    const id = spotIdInput.value;
    const occupied = occupiedCheckbox.checked;
    try {
      const res = await fetch(`/api/parking/${id}/occupy`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ occupied })
      });
      if (res.status === 403) {
        occupiedCheckbox.checked = !occupied; // revert UI
        throw new Error('Нет прав менять занятость');
      }
      if (!res.ok) {
        const txt = await res.text();
        occupiedCheckbox.checked = !occupied;
        throw new Error(txt || 'Ошибка сервера');
      }
      await loadSpots(); // обновляем состояние маркеров
    } catch (err) {
      modalError.style.display = 'block';
      modalError.textContent = err.message || String(err);
    }
  });


  /* ================= LOAD SPOTS ================= */

   async function loadSpots() {
      clearMarkers();
      const res = await fetch('/api/parking/all', { credentials: 'same-origin' });
      if (!res.ok) { console.error('Не удалось загрузить парковки'); return; }
      const spots = await res.json();

      spots.forEach(spot => {
        const lat = spot.cords.lat;
        const lng = spot.cords.lng;
        const color = colorByState(spot.state);

        const marker = L.circleMarker([lat, lng], {
          radius: 8,
          weight: 1,
          opacity: 1,
          fillOpacity: 0.9,
          color,
          fillColor: color
        });

        marker.on('click', async () => {
          try {
            const spotRes = await fetch(`/api/parking/${spot.id}`, { credentials: 'same-origin' });
            if (!spotRes.ok) throw new Error('Не удалось загрузить данные парковки');
            const fullSpot = await spotRes.json();
            openModal(fullSpot);
          } catch (err) {
            console.error(err);
            alert('Ошибка при загрузке данных парковки');
          }
        });

        marker.addTo(markersLayer);
      });
    }
  loadSpots();

  /* ================= ADD SPOT ================= */

  document.getElementById('addSpotForm').addEventListener('submit', async e => {
    e.preventDefault();

    const description = document.getElementById('desc').value;
    const lat = parseFloat(document.getElementById('lat').value);
    const lng = parseFloat(document.getElementById('lng').value);

    const body = {
      lat,
      lng,
      description
    };

    const res = await fetch('/api/parking/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      alert('Ошибка при добавлении парковки');
      return;
    }

    document.getElementById('addSpotForm').reset();
    await loadSpots();
  });

  /* ================= MAP EVENTS ================= */

  const radius = parseFloat(document.getElementById('searchRadius').value);
  document.getElementById('locateBtn').addEventListener('click', () => {
    const center = map.getCenter();
    document.getElementById('lat').value = center.lat.toFixed(6);
    document.getElementById('lng').value = center.lng.toFixed(6);
  });

 const controls = document.getElementById('controls');
  map.on('click', e => {
    document.getElementById('lat').value = e.latlng.lat.toFixed(6);
    document.getElementById('lng').value = e.latlng.lng.toFixed(6);
    document.getElementById('searchLat').value = e.latlng.lat.toFixed(6);
    document.getElementById('searchLng').value = e.latlng.lng.toFixed(6);
    if (window.isLoggedIn) {
        controls.classList.add('active');
    }
  });

  const closeBtn = document.getElementById('closeControlsBtn');
  closeBtn.addEventListener('click', () => {
      controls.classList.remove('active');
  });

const findParkingForm = document.getElementById('findParkingForm');
const searchResults = document.getElementById('searchResults');
let searchMarkers = [];

function clearSearchMarkers() {
    searchMarkers.forEach(marker => map.removeLayer(marker)); // удаляем с карты
    searchMarkers = []; // очищаем массив
}
findParkingForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const lat = parseFloat(document.getElementById('searchLat').value);
    const lng = parseFloat(document.getElementById('searchLng').value);
    const radius = parseFloat(document.getElementById('searchRadius').value);

    // Очистим старые результаты
    searchResults.innerHTML = '';
    clearSearchMarkers()
    try {
        const res = await fetch(`/api/parking/nearby?lat=${lat}&lng=${lng}&radius=${radius}`, {
            method: 'GET',
            credentials: 'same-origin'
        });

        if (!res.ok) throw new Error('Ошибка запроса');

        const data = await res.json();

        if (data.length === 0) {
            searchResults.textContent = 'Свободных парковок не найдено';
            return;
        }

        // Добавляем маркеры и список
        data.forEach(spot => {
            // Добавляем маркер на карту
            const marker = L.marker([spot.lat, spot.lng]).addTo(map)
                .bindPopup(spot.description);
            marker.on('click', async () => {
                      try {
                        const spotRes = await fetch(`/api/parking/${spot.id}`, { credentials: 'same-origin' });
                        if (!spotRes.ok) throw new Error('Не удалось загрузить данные парковки');
                        const fullSpot = await spotRes.json();
                        openModal(fullSpot);
                      } catch (err) {
                        console.error(err);
                        alert('Ошибка при загрузке данных парковки');
                      }
                    });
            searchMarkers.push(marker);

            // Создаём элемент в списке
            const div = document.createElement('div');
            div.className = 'parking-item';
            div.textContent = `${spot.description} (ID: ${spot.id})`;

            // При клике на элемент списка центрируем карту и открываем popup
            div.addEventListener('click', () => {
                map.setView([spot.lat, spot.lng], 18); // увеличиваем zoom
                marker.openPopup();
            });

            searchResults.appendChild(div);
        });

    } catch (err) {
        searchResults.textContent = 'Ошибка при поиске парковок';
        console.error(err);
    }
});

})();

