// Enhanced Three.js 3D Model with improved stability and interactivity
        let scene, camera, renderer, headband;
        let isDragging = false;
        let previousMousePosition = { x: 0, y: 0 };
        let rotation = { x: 0, y: 0 };
        let targetRotation = { x: 0, y: 0 };

        function initThreeJS() {
            try {
                const container = document.getElementById('threejs-container');
                const loading = document.getElementById('loading');
                
                if (!container) {
                    console.error('Container not found');
                    return;
                }

                container.innerHTML = '';
                
                // Scene setup with enhanced settings
                scene = new THREE.Scene();
                camera = new THREE.PerspectiveCamera(50, container.offsetWidth / container.offsetHeight, 0.1, 1000);
                renderer = new THREE.WebGLRenderer({ 
                    alpha: true, 
                    antialias: true,
                    powerPreference: "high-performance"
                });
                
                renderer.setSize(container.offsetWidth, container.offsetHeight);
                renderer.setClearColor(0x000000, 0);
                renderer.shadowMap.enabled = true;
                renderer.shadowMap.type = THREE.PCFSoftShadowMap;
                renderer.toneMapping = THREE.ACESFilmicToneMapping;
                renderer.toneMappingExposure = 1.2;
                
                container.appendChild(renderer.domElement);

                // Enhanced lighting setup
                const ambientLight = new THREE.AmbientLight(0x404040, 0.6);
                scene.add(ambientLight);
                
                const mainLight = new THREE.DirectionalLight(0x00ff88, 1.5);
                mainLight.position.set(4, 4, 4);
                mainLight.castShadow = true;
                mainLight.shadow.mapSize.width = 2048;
                mainLight.shadow.mapSize.height = 2048;
                scene.add(mainLight);
                
                const fillLight = new THREE.PointLight(0x00ccff, 0.8, 100);
                fillLight.position.set(-3, -2, 3);
                scene.add(fillLight);

                const rimLight = new THREE.DirectionalLight(0xffffff, 0.7);
                rimLight.position.set(-2, 1, -2);
                scene.add(rimLight);

                // Create enhanced headband model
                const group = new THREE.Group();
                
                // Main headband structure - more realistic proportions
                const mainBandGeometry = new THREE.TorusGeometry(2.2, 0.15, 20, 80);
                const mainBandMaterial = new THREE.MeshPhysicalMaterial({ 
                    color: 0x2a2a2a,
                    metalness: 0.3,
                    roughness: 0.4,
                    clearcoat: 0.3,
                    transparent: true,
                    opacity: 0.95
                });
                const mainBand = new THREE.Mesh(mainBandGeometry, mainBandMaterial);
                mainBand.rotation.x = Math.PI / 2;
                mainBand.castShadow = true;
                mainBand.receiveShadow = true;
                group.add(mainBand);

                // Inner comfort padding
                const paddingGeometry = new THREE.TorusGeometry(2.0, 0.08, 12, 40);
                const paddingMaterial = new THREE.MeshLambertMaterial({ 
                    color: 0x1a1a1a,
                    transparent: true,
                    opacity: 0.9
                });
                const padding = new THREE.Mesh(paddingGeometry, paddingMaterial);
                padding.rotation.x = Math.PI / 2;
                group.add(padding);

                // Enhanced sensor array with realistic positioning
                const sensorPositions = [
                    { angle: 0, name: 'frontal' },
                    { angle: Math.PI * 0.25, name: 'right' },
                    { angle: Math.PI * 0.75, name: 'back' },
                    { angle: Math.PI * 1.25, name: 'left' }
                ];

                sensorPositions.forEach((pos, i) => {
                    // Main sensor housing
                    const sensorGeometry = new THREE.CylinderGeometry(0.08, 0.08, 0.12, 12);
                    const sensorMaterial = new THREE.MeshPhysicalMaterial({ 
                        color: 0x333333,
                        metalness: 0.8,
                        roughness: 0.2,
                        emissive: 0x002211
                    });
                    const sensor = new THREE.Mesh(sensorGeometry, sensorMaterial);
                    sensor.position.x = Math.cos(pos.angle) * 2.3;
                    sensor.position.z = Math.sin(pos.angle) * 2.3;
                    sensor.position.y = 0.06;
                    sensor.rotation.x = Math.PI / 2;
                    sensor.castShadow = true;
                    group.add(sensor);

                    // Sensor contact point
                    const contactGeometry = new THREE.SphereGeometry(0.05, 12, 12);
                    const contactMaterial = new THREE.MeshPhongMaterial({ 
                        color: 0x00ff88,
                        emissive: 0x003322,
                        shininess: 100
                    });
                    const contact = new THREE.Mesh(contactGeometry, contactMaterial);
                    contact.position.copy(sensor.position);
                    contact.position.y += 0.08;
                    contact.castShadow = true;
                    group.add(contact);

                    // Glowing data transmission effect
                    const glowGeometry = new THREE.RingGeometry(0.1, 0.18, 16);
                    const glowMaterial = new THREE.MeshBasicMaterial({ 
                        color: 0x00ff88,
                        transparent: true,
                        opacity: 0.4,
                        side: THREE.DoubleSide
                    });
                    const glow = new THREE.Mesh(glowGeometry, glowMaterial);
                    glow.position.copy(contact.position);
                    glow.rotation.x = -Math.PI / 2;
                    group.add(glow);
                });

                // Enhanced control unit with detailed features
                const controlUnitGeometry = new THREE.BoxGeometry(0.4, 0.2, 0.8);
                const controlUnitMaterial = new THREE.MeshPhysicalMaterial({ 
                    color: 0x444444,
                    metalness: 0.6,
                    roughness: 0.3,
                    clearcoat: 0.5
                });
                const controlUnit = new THREE.Mesh(controlUnitGeometry, controlUnitMaterial);
                controlUnit.position.set(2.4, 0, 0);
                controlUnit.castShadow = true;
                group.add(controlUnit);

                // Display screen on control unit
                const screenGeometry = new THREE.PlaneGeometry(0.3, 0.15);
                const screenMaterial = new THREE.MeshBasicMaterial({ 
                    color: 0x001122,
                    emissive: 0x003344
                });
                const screen = new THREE.Mesh(screenGeometry, screenMaterial);
                screen.position.set(2.51, 0, 0);
                screen.rotation.y = Math.PI / 2;
                group.add(screen);

                // Charging port
                const portGeometry = new THREE.BoxGeometry(0.06, 0.03, 0.12);
                const portMaterial = new THREE.MeshBasicMaterial({ color: 0x000000 });
                const port = new THREE.Mesh(portGeometry, portMaterial);
                port.position.set(2.2, -0.08, 0.25);
                group.add(port);

                // Status LEDs
                for (let i = 0; i < 3; i++) {
                    const ledGeometry = new THREE.SphereGeometry(0.025, 8, 8);
                    const ledColors = [0x00ff00, 0xffff00, 0xff0000];
                    const ledMaterial = new THREE.MeshBasicMaterial({ 
                        color: ledColors[i],
                        transparent: true,
                        opacity: 0.8
                    });
                    const led = new THREE.Mesh(ledGeometry, ledMaterial);
                    led.position.set(2.45, 0.08, -0.1 + (i * 0.1));
                    group.add(led);
                }

                // Enhanced connecting elements
                const wireGeometry = new THREE.CylinderGeometry(0.015, 0.015, 0.5, 8);
                const wireMaterial = new THREE.MeshLambertMaterial({ color: 0x222222 });
                const wire1 = new THREE.Mesh(wireGeometry, wireMaterial);
                wire1.position.set(2.0, 0, 0.2);
                wire1.rotation.z = Math.PI / 4;
                group.add(wire1);

                const wire2 = new THREE.Mesh(wireGeometry, wireMaterial);
                wire2.position.set(2.0, 0, -0.2);
                wire2.rotation.z = -Math.PI / 4;
                group.add(wire2);

                // Add sports-themed details
                const logoGeometry = new THREE.RingGeometry(0.08, 0.12, 8);
                const logoMaterial = new THREE.MeshBasicMaterial({ 
                    color: 0x00ff88,
                    transparent: true,
                    opacity: 0.7,
                    side: THREE.DoubleSide
                });
                const logo = new THREE.Mesh(logoGeometry, logoMaterial);
                logo.position.set(2.52, 0, 0);
                logo.rotation.y = Math.PI / 2;
                group.add(logo);

                headband = group;
                scene.add(headband);

                // Position camera for optimal view
                camera.position.set(4, 2, 5);
                camera.lookAt(0, 0, 0);

                // Remove loading indicator
                if (loading) {
                    loading.style.display = 'none';
                }

                // Enhanced mouse/touch interaction
                setupInteractions(container);
                
                console.log('Enhanced Three.js model initialized successfully');
                animate();
                
            } catch (error) {
                console.error('Three.js initialization failed:', error);
                const container = document.getElementById('threejs-container');
                container.innerHTML = `
                    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #00ff88;">
                        <div style="font-size: 4rem; margin-bottom: 1rem;">🎧</div>
                        <div style="font-size: 1.2rem; text-align: center;">
                            KAVLO Smart Headband<br>
                            <span style="font-size: 0.9rem; opacity: 0.7;">3D Model Preview</span>
                        </div>
                    </div>
                `;
            }
        }

        function setupInteractions(container) {
            // Mouse events
            container.addEventListener('mousedown', onMouseDown, false);
            container.addEventListener('mousemove', onMouseMove, false);
            container.addEventListener('mouseup', onMouseUp, false);
            container.addEventListener('wheel', onMouseWheel, false);
            
            // Touch events for mobile
            container.addEventListener('touchstart', onTouchStart, false);
            container.addEventListener('touchmove', onTouchMove, false);
            container.addEventListener('touchend', onTouchEnd, false);
        }

        function onMouseDown(event) {
            isDragging = true;
            previousMousePosition.x = event.clientX;
            previousMousePosition.y = event.clientY;
            document.getElementById('threejs-container').style.cursor = 'grabbing';
        }

        function onMouseMove(event) {
            if (!isDragging) return;
            
            const deltaMove = {
                x: event.clientX - previousMousePosition.x,
                y: event.clientY - previousMousePosition.y
            };

            targetRotation.y += deltaMove.x * 0.01;
            targetRotation.x += deltaMove.y * 0.01;
            
            // Limit vertical rotation
            targetRotation.x = Math.max(-Math.PI / 3, Math.min(Math.PI / 3, targetRotation.x));

            previousMousePosition.x = event.clientX;
            previousMousePosition.y = event.clientY;
        }

        function onMouseUp() {
            isDragging = false;
            document.getElementById('threejs-container').style.cursor = 'grab';
        }

        function onMouseWheel(event) {
            event.preventDefault();
            const zoomSpeed = 0.1;
            const minDistance = 3;
            const maxDistance = 10;
            
            camera.position.multiplyScalar(1 + event.deltaY * 0.001);
            
            const distance = camera.position.length();
            if (distance < minDistance) {
                camera.position.normalize().multiplyScalar(minDistance);
            } else if (distance > maxDistance) {
                camera.position.normalize().multiplyScalar(maxDistance);
            }
        }

        function onTouchStart(event) {
            if (event.touches.length === 1) {
                isDragging = true;
                const touch = event.touches[0];
                previousMousePosition.x = touch.clientX;
                previousMousePosition.y = touch.clientY;
            }
        }

        function onTouchMove(event) {
            event.preventDefault();
            if (!isDragging || event.touches.length !== 1) return;
            
            const touch = event.touches[0];
            const deltaMove = {
                x: touch.clientX - previousMousePosition.x,
                y: touch.clientY - previousMousePosition.y
            };

            targetRotation.y += deltaMove.x * 0.01;
            targetRotation.x += deltaMove.y * 0.01;
            
            targetRotation.x = Math.max(-Math.PI / 3, Math.min(Math.PI / 3, targetRotation.x));

            previousMousePosition.x = touch.clientX;
            previousMousePosition.y = touch.clientY;
        }

        function onTouchEnd() {
            isDragging = false;
        }

        function animate() {
            requestAnimationFrame(animate);
            
            if (headband && renderer && camera && scene) {
                // Smooth rotation interpolation
                rotation.x += (targetRotation.x - rotation.x) * 0.1;
                rotation.y += (targetRotation.y - rotation.y) * 0.1;
                
                headband.rotation.x = rotation.x;
                headband.rotation.y = rotation.y;
                
                // Animate sensors with realistic pulsing
                const time = Date.now() * 0.002;
                let sensorIndex = 0;
                
                headband.children.forEach((child, index) => {
                    // Animate sensor contacts (glowing spheres)
                    if (child.geometry && child.geometry.type === 'SphereGeometry') {
                        const pulse = Math.sin(time + sensorIndex * 1.5) * 0.15 + 0.85;
                        child.scale.setScalar(pulse);
                        
                        if (child.material.emissive) {
                            const intensity = (Math.sin(time + sensorIndex * 1.2) + 1) * 0.5;
                            child.material.emissive.setRGB(0, intensity * 0.3, intensity * 0.1);
                        }
                        sensorIndex++;
                    }
                    
                    // Animate glow rings
                    if (child.geometry && child.geometry.type === 'RingGeometry' && child.material.opacity) {
                        const glowPulse = (Math.sin(time * 1.5 + index * 0.8) + 1) * 0.3 + 0.1;
                        child.material.opacity = glowPulse;
                        child.rotation.z += 0.005;
                    }
                });
                
                // Animate status LEDs with different patterns
                const ledElements = headband.children.filter(child => 
                    child.geometry && child.geometry.type === 'SphereGeometry' && child.position.x > 2
                );
                
                ledElements.forEach((led, i) => {
                    const ledTime = Date.now() * (0.003 + i * 0.001);
                    const brightness = (Math.sin(ledTime) + 1) * 0.5;
                    led.material.opacity = 0.4 + brightness * 0.6;
                });
                
                // Subtle floating animation when not dragging
                if (!isDragging) {
                    const floatY = Math.sin(time * 0.5) * 0.1;
                    headband.position.y = floatY;
                }
                
                camera.lookAt(headband.position);
                renderer.render(scene, camera);
            }
        }

        // Matrix-style sports background
        function createMatrixBackground() {
            const matrixBg = document.getElementById('matrixBg');
            const sportsSymbols = ['⚽', '🏀', '🏈', '🎾', '🏐', '🏓', '🏸', '⚾', '🥎', '🏒', '🏑', '🥍', '🏏', '⛳', '🏹', '🎯', '🥊', '🤺', '🏊', '🚴', '🤸', '🏃', '💪', '🏆', '🥇', '⏱️', '📊', '💯', '🔥', '⚡'];
            
            function createColumn() {
                const column = document.createElement('div');
                column.className = 'matrix-column';
                column.style.left = Math.random() * 100 + '%';
                column.style.animationDuration = (8 + Math.random() * 6) + 's';
                column.style.animationDelay = Math.random() * 2 + 's';
                
                let text = '';
                for (let i = 0; i < 20; i++) {
                    if (Math.random() > 0.7) {
                        text += sportsSymbols[Math.floor(Math.random() * sportsSymbols.length)];
                    } else {
                        text += String.fromCharCode(0x30A0 + Math.floor(Math.random() * 96));
                    }
                    text += '\n';
                }
                column.textContent = text;
                
                matrixBg.appendChild(column);
                
                setTimeout(() => {
                    if (column.parentNode) {
                        column.remove();
                    }
                }, 14000);
            }
            
            // Create initial columns
            for (let i = 0; i < 15; i++) {
                setTimeout(() => createColumn(), i * 500);
            }
            
            // Continue creating columns
            setInterval(createColumn, 1500);
        }

        // Enhanced floating particles with sports theme
        function createSportsParticles() {
            const particles = document.getElementById('particles');
            
            function addParticle() {
                const isSpecial = Math.random() > 0.7;
                const particle = document.createElement('div');
                
                if (isSpecial) {
                    particle.className = 'pulse-particle';
                    particle.style.left = Math.random() * 100 + 'vw';
                    particle.style.animationDelay = Math.random() * 4 + 's';
                } else {
                    particle.className = 'particle';
                    particle.style.left = Math.random() * 100 + 'vw';
                    particle.style.animationDelay = Math.random() * 6 + 's';
                    particle.style.animationDuration = (6 + Math.random() * 4) + 's';
                }
                
                particles.appendChild(particle);
                
                setTimeout(() => {
                    if (particle.parentNode) {
                        particle.remove();
                    }
                }, isSpecial ? 12000 : 10000);
            }
            
            setInterval(addParticle, 1500);
        }

        function toggleMenu() {
            const navLinks = document.getElementById('navLinks');
            navLinks.classList.toggle('active');
        }

        function downloadApp(platform) {
            const messages = {
                ios: "Redirecting to App Store...\n\nKAVLO for iOS\nVersion 2.1.0\nCompatible with iOS 15.0 or later\nSize: 127 MB",
                android: "Redirecting to Google Play Store...\n\nKAVLO for Android\nVersion 2.1.2\nCompatible with Android 8.0 or later\nSize: 95 MB"
            };
            
            alert(messages[platform]);
        }

        // Smooth scrolling with enhanced easing
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });

        // Dynamic navbar effects
        window.addEventListener('scroll', () => {
            const navbar = document.querySelector('.navbar');
            const scrollY = window.scrollY;
            
            if (scrollY > 100) {
                navbar.style.background = 'rgba(0, 0, 0, 0.95)';
                navbar.style.borderBottom = '1px solid rgba(0, 255, 136, 0.3)';
            } else {
                navbar.style.background = 'rgba(0, 0, 0, 0.9)';
                navbar.style.borderBottom = '1px solid rgba(0, 255, 136, 0.1)';
            }
        });

        // Enhanced window resize handling
        window.addEventListener('resize', () => {
            if (renderer && camera) {
                const container = document.getElementById('threejs-container');
                const width = container.offsetWidth;
                const height = container.offsetHeight;
                
                camera.aspect = width / height;
                camera.updateProjectionMatrix();
                renderer.setSize(width, height);
            }
        });

        // Performance optimization for mobile
        function optimizeForDevice() {
            const isMobile = window.innerWidth < 768;
            if (renderer) {
                renderer.setPixelRatio(isMobile ? Math.min(window.devicePixelRatio, 2) : window.devicePixelRatio);
            }
        }

        // Initialize everything when page loads
        document.addEventListener('DOMContentLoaded', () => {
            console.log('DOM loaded, initializing enhanced experience...');
            setTimeout(() => {
                initThreeJS();
                createSportsParticles();
                createMatrixBackground();
                optimizeForDevice();
            }, 100);
        });

        // Fallback initialization
        window.addEventListener('load', () => {
            if (!scene) {
                console.log('Fallback initialization...');
                setTimeout(() => {
                    initThreeJS();
                    createSportsParticles();
                    createMatrixBackground();
                }, 200);
            }
        });

        // Add keyboard shortcuts for enhanced interaction
        document.addEventListener('keydown', (event) => {
            if (!headband) return;
            
            switch(event.key) {
                case 'r':
                case 'R':
                    // Reset rotation
                    targetRotation.x = 0;
                    targetRotation.y = 0;
                    camera.position.set(4, 2, 5);
                    break;
                case 'ArrowLeft':
                    targetRotation.y -= 0.2;
                    break;
                case 'ArrowRight':
                    targetRotation.y += 0.2;
                    break;
                case 'ArrowUp':
                    targetRotation.x -= 0.2;
                    break;
                case 'ArrowDown':
                    targetRotation.x += 0.2;
                    break;
            }
        });