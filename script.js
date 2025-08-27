        // Three.js 3D Model Setup
        let scene, camera, renderer, headband, isRotating = true;
        let mouseX = 0, mouseY = 0;

        function initThreeJS() {
            try {
                const container = document.getElementById('threejs-container');
                const loading = document.getElementById('loading');
                
                if (!container) {
                    console.error('Container not found');
                    return;
                }

                // Clear any existing content
                container.innerHTML = '';
                
                // Scene setup
                scene = new THREE.Scene();
                camera = new THREE.PerspectiveCamera(75, container.offsetWidth / container.offsetHeight, 0.1, 1000);
                renderer = new THREE.WebGLRenderer({ 
                    alpha: true, 
                    antialias: true,
                    powerPreference: "high-performance"
                });
                
                renderer.setSize(container.offsetWidth, container.offsetHeight);
                renderer.setClearColor(0x000000, 0);
                renderer.shadowMap.enabled = true;
                renderer.shadowMap.type = THREE.PCFSoftShadowMap;
                
                container.appendChild(renderer.domElement);

                // Enhanced Lighting
                const ambientLight = new THREE.AmbientLight(0x404040, 0.4);
                scene.add(ambientLight);
                
                const directionalLight = new THREE.DirectionalLight(0x00ff88, 1.2);
                directionalLight.position.set(5, 5, 5);
                directionalLight.castShadow = true;
                scene.add(directionalLight);
                
                const pointLight = new THREE.PointLight(0x00ccff, 0.8, 100);
                pointLight.position.set(-5, -5, 5);
                scene.add(pointLight);

                const rimLight = new THREE.DirectionalLight(0xffffff, 0.5);
                rimLight.position.set(-5, 0, -5);
                scene.add(rimLight);

                // Create detailed headband geometry
                const group = new THREE.Group();
                
                // Main headband ring - using torus
                const ringGeometry = new THREE.TorusGeometry(1.8, 0.12, 16, 64);
                const ringMaterial = new THREE.MeshLambertMaterial({ 
                    color: 0x2a2a2a,
                    transparent: true,
                    opacity: 0.95
                });
                const ring = new THREE.Mesh(ringGeometry, ringMaterial);
                ring.rotation.x = Math.PI / 2;
                ring.castShadow = true;
                ring.receiveShadow = true;
                group.add(ring);

                // Inner padding ring
                const paddingGeometry = new THREE.TorusGeometry(1.6, 0.08, 8, 32);
                const paddingMaterial = new THREE.MeshLambertMaterial({ 
                    color: 0x1a1a1a,
                    transparent: true,
                    opacity: 0.8
                });
                const padding = new THREE.Mesh(paddingGeometry, paddingMaterial);
                padding.rotation.x = Math.PI / 2;
                group.add(padding);

                // EEG Sensors (glowing spheres)
                for (let i = 0; i < 4; i++) {
                    const angle = (i / 8) * Math.PI * 2;
                    
                    // Main sensor
                    const sensorGeometry = new THREE.SphereGeometry(0.06, 12, 12);
                    const sensorMaterial = new THREE.MeshPhongMaterial({ 
                        color: 0x00ff88,
                        emissive: 0x002211,
                        shininess: 100
                    });
                    const sensor = new THREE.Mesh(sensorGeometry, sensorMaterial);
                    sensor.position.x = Math.cos(angle) * 1.9;
                    sensor.position.z = Math.sin(angle) * 1.9;
                    sensor.position.y = 0.05;
                    sensor.castShadow = true;
                    group.add(sensor);

                    // Glowing halo effect
                    const haloGeometry = new THREE.RingGeometry(0.08, 0.15, 8);
                    const haloMaterial = new THREE.MeshBasicMaterial({ 
                        color: 0x00ff88,
                        transparent: true,
                        opacity: 0.3,
                        side: THREE.DoubleSide
                    });
                    const halo = new THREE.Mesh(haloGeometry, haloMaterial);
                    halo.position.copy(sensor.position);
                    halo.rotation.x = -Math.PI / 2;
                    group.add(halo);
                }

                // Control unit (more detailed)
                const controlGeometry = new THREE.BoxGeometry(0.3, 0.15, 0.6);
                const controlMaterial = new THREE.MeshPhongMaterial({ 
                    color: 0x444444,
                    shininess: 90
                });
                const control = new THREE.Mesh(controlGeometry, controlMaterial);
                control.position.set(2.0, 0, 0);
                control.castShadow = true;
                group.add(control);

                // USB-C port detail
                const portGeometry = new THREE.BoxGeometry(0.05, 0.02, 0.1);
                const portMaterial = new THREE.MeshBasicMaterial({ color: 0x000000 });
                const port = new THREE.Mesh(portGeometry, portMaterial);
                port.position.set(2.15, -0.06, 0);
                group.add(port);

                // Status LED
                const ledGeometry = new THREE.SphereGeometry(0.03, 8, 8);
                const ledMaterial = new THREE.MeshBasicMaterial({ 
                    color: 0x00ccff,
                    transparent: true,
                    opacity: 0.9
                });
                const led = new THREE.Mesh(ledGeometry, ledMaterial);
                led.position.set(2.1, 0.08, 0.15);
                group.add(led);

                // Connecting cable
                const cablePoints = [];
                for (let i = 0; i <= 10; i++) {
                    const t = i / 10;
                    const x = 1.8 * Math.cos(t * Math.PI * 0.3) + (2.0 - 1.8) * t;
                    const y = Math.sin(t * Math.PI * 2) * 0.1;
                    const z = 0;
                    cablePoints.push(new THREE.Vector3(x, y, z));
                }
                
                const cableGeometry = new THREE.TubeGeometry(
                    new THREE.CatmullRomCurve3(cablePoints), 
                    20, 
                    0.02, 
                    8, 
                    false
                );
                const cableMaterial = new THREE.MeshLambertMaterial({ color: 0x333333 });
                const cable = new THREE.Mesh(cableGeometry, cableMaterial);
                group.add(cable);

                headband = group;
                scene.add(headband);

                // Position camera for better view
                camera.position.set(3, 2, 4);
                camera.lookAt(0, 0, 0);

                // Remove loading indicator
                if (loading) {
                    loading.style.display = 'none';
                }

                // Add mouse/touch interaction
                container.addEventListener('mousemove', onMouseMove, false);
                container.addEventListener('touchmove', onTouchMove, false);
                
                console.log('Three.js initialized successfully');
                animate();
                
            } catch (error) {
                console.error('Three.js initialization failed:', error);
                const container = document.getElementById('threejs-container');
                container.innerHTML = `
                    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #00ff88;">
                        <div style="font-size: 4rem; margin-bottom: 1rem;">🎧</div>
                        <div style="font-size: 1.2rem; text-align: center;">
                            NeuralSync Pro Headband<br>
                            <span style="font-size: 0.9rem; opacity: 0.7;">3D Model Preview</span>
                        </div>
                    </div>
                `;
            }
        }

        function onMouseMove(event) {
            const container = document.getElementById('threejs-container');
            const rect = container.getBoundingClientRect();
            mouseX = (event.clientX - rect.left - rect.width / 2) / (rect.width / 2);
            mouseY = (event.clientY - rect.top - rect.height / 2) / (rect.height / 2);
        }

        function onTouchMove(event) {
            if (event.touches.length > 0) {
                const container = document.getElementById('threejs-container');
                const rect = container.getBoundingClientRect();
                const touch = event.touches[0];
                mouseX = (touch.clientX - rect.left - rect.width / 2) / (rect.width / 2);
                mouseY = (touch.clientY - rect.top - rect.height / 2) / (rect.height / 2);
            }
        }

        function animate() {
            requestAnimationFrame(animate);
            
            if (headband && renderer && camera && scene) {
                if (isRotating) {
                    headband.rotation.y += 0.008;
                }
                
                // Smooth mouse interaction
                if (mouseX !== undefined && mouseY !== undefined) {
                    headband.rotation.x += (mouseY * 0.3 - headband.rotation.x) * 0.05;
                    headband.rotation.y += (mouseX * 0.3 + Date.now() * 0.0001) * 0.05;
                }
                
                // Animate sensors with pulsing effect
                const time = Date.now() * 0.003;
                headband.children.forEach((child, index) => {
                    if (child.geometry && child.geometry.type === 'SphereGeometry' && index < 20) {
                        const pulse = Math.sin(time + index * 0.5) * 0.02;
                        child.scale.setScalar(1 + pulse);
                        
                        if (child.material.emissive) {
                            const intensity = (Math.sin(time + index * 0.3) + 1) * 0.5;
                            child.material.emissive.setHex(intensity > 0.5 ? 0x003322 : 0x001111);
                        }
                    }
                });
                
                // Animate LED
                const ledIndex = headband.children.length - 1;
                const led = headband.children[ledIndex];
                if (led && led.material && led.material.emissive) {
                    const ledTime = Date.now() * 0.005;
                    led.material.emissive.setHex(Math.sin(ledTime) > 0 ? 0x0066cc : 0x003366);
                }
                
                renderer.render(scene, camera);
            }
        }

        function toggleRotation() {
            isRotating = !isRotating;
            const btn = document.querySelector('.control-btn');
            btn.innerHTML = isRotating ? '⏸️ Pause' : '▶️ Play';
        }

        function toggleWireframe() {
            if (headband) {
                headband.children.forEach(child => {
                    if (child.material) {
                        child.material.wireframe = !child.material.wireframe;
                    }
                });
            }
        }

        function resetCamera() {
            camera.position.set(0, 2, 5);
            camera.lookAt(0, 0, 0);
        }

        function toggleMenu() {
            const navLinks = document.getElementById('navLinks');
            navLinks.classList.toggle('active');
        }

        function downloadApp(platform) {
            const messages = {
                ios: "Redirecting to App Store...\n\nNeuralSync Pro for iOS\nVersion 2.1.0\nCompatible with iOS 15.0 or later\nSize: 127 MB",
                android: "Redirecting to Google Play Store...\n\nNeuralSync Pro for Android\nVersion 2.1.2\nCompatible with Android 8.0 or later\nSize: 95 MB"
            };
            
            alert(messages[platform]);
            
            // In a real app, these would be actual store links
            setTimeout(() => {
                if (platform === 'ios') {
                    // window.open('https://apps.apple.com/app/neuralsync-pro');
                } else {
                    // window.open('https://play.google.com/store/apps/details?id=com.neuralsync.pro');
                }
            }, 1000);
        }

        // Floating particles animation
        function createParticles() {
            const particles = document.getElementById('particles');
            
            function addParticle() {
                const particle = document.createElement('div');
                particle.className = 'particle';
                particle.style.left = Math.random() * 100 + 'vw';
                particle.style.animationDelay = Math.random() * 6 + 's';
                particle.style.animationDuration = (6 + Math.random() * 4) + 's';
                particles.appendChild(particle);
                
                setTimeout(() => {
                    particle.remove();
                }, 10000);
            }
            
            setInterval(addParticle, 2000);
        }

        // Smooth scrolling
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

        // Navbar background on scroll
        window.addEventListener('scroll', () => {
            const navbar = document.querySelector('.navbar');
            if (window.scrollY > 100) {
                navbar.style.background = 'rgba(0, 0, 0, 0.95)';
            } else {
                navbar.style.background = 'rgba(0, 0, 0, 0.9)';
            }
        });

        // Handle window resize
        window.addEventListener('resize', () => {
            if (renderer && camera) {
                const container = document.getElementById('threejs-container');
                camera.aspect = container.offsetWidth / container.offsetHeight;
                camera.updateProjectionMatrix();
                renderer.setSize(container.offsetWidth, container.offsetHeight);
            }
        });

        // Initialize everything when page loads
        document.addEventListener('DOMContentLoaded', () => {
            console.log('DOM loaded, initializing Three.js...');
            // Small delay to ensure container is properly sized
            setTimeout(() => {
                initThreeJS();
                createParticles();
            }, 100);
        });

        // Fallback for older browsers
        window.addEventListener('load', () => {
            if (!scene) {
                console.log('Fallback initialization...');
                setTimeout(() => {
                    initThreeJS();
                    createParticles();
                }, 200);
            }
        });