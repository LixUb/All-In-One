class KavloWebsite {
    constructor() {
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.headband = null;
        this.isDragging = false;
        this.previousMousePosition = { x: 0, y: 0 };
        this.rotation = { x: 0, y: 0 };
        this.targetRotation = { x: 0, y: 0 };
        this.animationId = null;
        this.chatbotOpen = false;
        this.isTyping = false;
        
        this.init();
    }

    init() {
        this.createLoadingScreen();
        this.createAnimatedBackground();
        this.initNavigation();
        this.initThreeJS();
        this.initChatbot();
        this.initScrollEffects();
        this.initAccessibility();
        this.createMatrixBackground();
        this.createSportsParticles();
        this.createAdvancedParticles();
        this.addInteractionFeedback();
        
        // Performance monitoring
        if ('performance' in window) {
            window.addEventListener('load', () => {
                const loadTime = performance.timing.loadEventEnd - performance.timing.navigationStart;
                console.log(`KAVLO site loaded in ${loadTime}ms`);
            });
        }
    }

    createLoadingScreen() {
        const loadingScreen = document.createElement('div');
        loadingScreen.id = 'loadingScreen';
        loadingScreen.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: #000;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            z-index: 10001;
            transition: opacity 0.5s ease;
        `;
        
        loadingScreen.innerHTML = `
            <div style="font-size: 3rem; margin-bottom: 2rem; background: linear-gradient(45deg, #00ff88, #00ccff); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; font-weight: bold;">KAVLO</div>
            <div style="width: 200px; height: 4px; background: rgba(0, 255, 136, 0.2); border-radius: 2px; overflow: hidden;">
                <div style="width: 0%; height: 100%; background: linear-gradient(45deg, #00ff88, #00ccff); border-radius: 2px; animation: loadingBar 3s ease-in-out forwards;"></div>
            </div>
            <div style="margin-top: 1rem; color: #00ff88; font-size: 1rem;">Initializing Smart Technology...</div>
        `;
        
        // Add loading animation
        const style = document.createElement('style');
        style.textContent = `
            @keyframes loadingBar {
                0% { width: 0%; }
                100% { width: 100%; }
            }
        `;
        document.head.appendChild(style);
        
        document.body.appendChild(loadingScreen);
        
        // Remove loading screen after 3.5 seconds
        setTimeout(() => {
            loadingScreen.style.opacity = '0';
            setTimeout(() => {
                if (loadingScreen.parentNode) {
                    loadingScreen.remove();
                }
            }, 500);
        }, 3500);
    }

    createAnimatedBackground() {
        const bg = document.getElementById('animatedBg');
        if (!bg) return;

        // Create optimized particle system
        for (let i = 0; i < 15; i++) {
            const particle = document.createElement('div');
            particle.className = 'bg-particle';
            particle.style.left = Math.random() * 100 + '%';
            particle.style.animationDelay = Math.random() * 15 + 's';
            particle.style.animationDuration = (15 + Math.random() * 10) + 's';
            bg.appendChild(particle);
        }
    }

    createMatrixBackground() {
        const matrixBg = document.getElementById('matrixBg');
        if (!matrixBg) return;
        
        const sportsSymbols = ['⚽', '🏀', '🏈', '🎾', '🏐', '🏓', '🏸', '⚾', '🥎', '💪', '🏃', '🚴', '🤸', '🏊', '🏆', '🥇', '⚡', '🔥'];
        
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

    createSportsParticles() {
        const particles = document.getElementById('particles');
        if (!particles) return;
        
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

    createAdvancedParticles() {
        const particles = document.getElementById('particles');
        if (!particles) return;
        
        function createDataStreamParticle() {
            const particle = document.createElement('div');
            particle.className = 'data-stream-particle';
            particle.style.cssText = `
                position: absolute;
                width: 1px;
                height: 20px;
                background: linear-gradient(to bottom, transparent, #00ff88, transparent);
                left: ${Math.random() * 100}vw;
                top: -50px;
                animation: dataStream ${4 + Math.random() * 3}s linear infinite;
            `;
            
            particles.appendChild(particle);
            
            setTimeout(() => {
                if (particle.parentNode) {
                    particle.remove();
                }
            }, 7000);
        }
        
        // Add CSS animation for data stream
        if (!document.querySelector('#dataStreamStyle')) {
            const style = document.createElement('style');
            style.id = 'dataStreamStyle';
            style.textContent = `
                @keyframes dataStream {
                    0% {
                        transform: translateY(0) scaleY(0);
                        opacity: 0;
                    }
                    10% {
                        opacity: 1;
                        transform: scaleY(1);
                    }
                    90% {
                        opacity: 1;
                    }
                    100% {
                        transform: translateY(100vh) scaleY(0);
                        opacity: 0;
                    }
                }
            `;
            document.head.appendChild(style);
        }
        
        setInterval(createDataStreamParticle, 2000);
    }

    initNavigation() {
        const navbar = document.getElementById('navbar');
        const hamburger = document.getElementById('hamburger');
        const navLinks = document.getElementById('navLinks');
        let lastScrollY = 0;

        // Enhanced scroll effects
        window.addEventListener('scroll', () => {
            const currentScrollY = window.scrollY;
            
            // Add scrolled class
            navbar.classList.toggle('scrolled', currentScrollY > 100);
            
            // Dynamic background based on scroll
            if (currentScrollY > 100) {
                navbar.style.background = 'rgba(0, 0, 0, 0.95)';
                navbar.style.backdropFilter = 'blur(20px)';
            } else {
                navbar.style.background = 'rgba(0, 0, 0, 0.9)';
                navbar.style.backdropFilter = 'blur(15px)';
            }
            
            // Hide/show navbar based on scroll direction
            if (currentScrollY > lastScrollY && currentScrollY > 200) {
                navbar.style.transform = 'translateY(-100%)';
            } else {
                navbar.style.transform = 'translateY(0)';
            }
            
            lastScrollY = currentScrollY;
        }, { passive: true });

        // Mobile menu toggle
        hamburger?.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navLinks.classList.toggle('active');
        });

        // Enhanced smooth scrolling for navigation links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', (e) => {
                e.preventDefault();
                const target = document.querySelector(anchor.getAttribute('href'));
                if (target) {
                    const offsetTop = target.offsetTop - 80;
                    window.scrollTo({ 
                        top: offsetTop,
                        behavior: 'smooth'
                    });
                    // Close mobile menu
                    hamburger?.classList.remove('active');
                    navLinks?.classList.remove('active');
                }
            });
        });
    }

    initThreeJS() {
        try {
            const container = document.getElementById('threejs-container');
            const loading = document.getElementById('loading');
            
            if (!container) return;

            // Scene setup with enhanced settings
            this.scene = new THREE.Scene();
            this.camera = new THREE.PerspectiveCamera(50, container.offsetWidth / container.offsetHeight, 0.1, 1000);
            this.renderer = new THREE.WebGLRenderer({ 
                alpha: true, 
                antialias: window.devicePixelRatio < 2,
                powerPreference: "high-performance"
            });
            
            this.renderer.setSize(container.offsetWidth, container.offsetHeight);
            this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
            this.renderer.setClearColor(0x000000, 0);
            this.renderer.shadowMap.enabled = true;
            this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
            this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
            this.renderer.toneMappingExposure = 1.2;
            
            container.appendChild(this.renderer.domElement);

            // Enhanced lighting system
            const ambientLight = new THREE.AmbientLight(0x404040, 0.6);
            this.scene.add(ambientLight);
            
            const mainLight = new THREE.DirectionalLight(0x00ff88, 1.5);
            mainLight.position.set(4, 4, 4);
            mainLight.castShadow = true;
            mainLight.shadow.mapSize.setScalar(2048);
            this.scene.add(mainLight);
            
            const fillLight = new THREE.PointLight(0x00ccff, 0.8, 100);
            fillLight.position.set(-3, -2, 3);
            this.scene.add(fillLight);

            const rimLight = new THREE.DirectionalLight(0xffffff, 0.7);
            rimLight.position.set(-2, 1, -2);
            this.scene.add(rimLight);

            // Create enhanced headband model
            this.createHeadbandModel();
            
            // Camera position
            this.camera.position.set(4, 2, 5);
            this.camera.lookAt(0, 0, 0);

            // Setup interactions
            this.setupThreeJSInteractions(container);
            
            // Hide loading
            if (loading) loading.style.display = 'none';
            
            // Start animation
            this.animate();
            
        } catch (error) {
            console.error('Three.js initialization failed:', error);
            this.showFallbackModel();
        }
    }

    createHeadbandModel() {
        const group = new THREE.Group();
        
        // Main headband with realistic proportions
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

        // Enhanced sensor array
        const sensorPositions = [
            { angle: 0, name: 'frontal' },
            { angle: Math.PI * 0.25, name: 'right' },
            { angle: Math.PI * 0.75, name: 'back' },
            { angle: Math.PI * 1.25, name: 'left' }
        ];

        sensorPositions.forEach((pos, i) => {
            // Sensor housing
            const sensorGeometry = new THREE.CylinderGeometry(0.08, 0.08, 0.12, 12);
            const sensorMaterial = new THREE.MeshPhysicalMaterial({ 
                color: 0x333333,
                metalness: 0.8,
                roughness: 0.2,
                emissive: 0x002211
            });
            const sensor = new THREE.Mesh(sensorGeometry, sensorMaterial);
            sensor.position.set(
                Math.cos(pos.angle) * 2.3,
                0.06,
                Math.sin(pos.angle) * 2.3
            );
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

        // Enhanced control unit
        const controlGeometry = new THREE.BoxGeometry(0.4, 0.2, 0.8);
        const controlMaterial = new THREE.MeshPhysicalMaterial({ 
            color: 0x444444,
            metalness: 0.6,
            roughness: 0.3,
            clearcoat: 0.5
        });
        const controlUnit = new THREE.Mesh(controlGeometry, controlMaterial);
        controlUnit.position.set(2.4, 0, 0);
        controlUnit.castShadow = true;
        group.add(controlUnit);

        // Display screen
        const screenGeometry = new THREE.PlaneGeometry(0.3, 0.15);
        const screenMaterial = new THREE.MeshBasicMaterial({ 
            color: 0x001122,
            emissive: 0x003344
        });
        const screen = new THREE.Mesh(screenGeometry, screenMaterial);
        screen.position.set(2.51, 0, 0);
        screen.rotation.y = Math.PI / 2;
        group.add(screen);

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

        this.headband = group;
        this.scene.add(this.headband);
    }

    setupThreeJSInteractions(container) {
        // Mouse events
        container.addEventListener('mousedown', this.onPointerDown.bind(this));
        container.addEventListener('mousemove', this.onPointerMove.bind(this));
        container.addEventListener('mouseup', this.onPointerUp.bind(this));
        container.addEventListener('wheel', this.onWheel.bind(this), { passive: false });
        
        // Touch events
        container.addEventListener('touchstart', this.onPointerDown.bind(this), { passive: true });
        container.addEventListener('touchmove', this.onPointerMove.bind(this), { passive: true });
        container.addEventListener('touchend', this.onPointerUp.bind(this), { passive: true });

        // Set initial cursor style
        container.style.cursor = 'grab';
    }

    onPointerDown(event) {
        this.isDragging = true;
        const clientX = event.clientX || (event.touches && event.touches[0].clientX);
        const clientY = event.clientY || (event.touches && event.touches[0].clientY);
        this.previousMousePosition = { x: clientX, y: clientY };
        document.getElementById('threejs-container').style.cursor = 'grabbing';
    }

    onPointerMove(event) {
        if (!this.isDragging) return;
        
        const clientX = event.clientX || (event.touches && event.touches[0].clientX);
        const clientY = event.clientY || (event.touches && event.touches[0].clientY);
        
        const deltaMove = {
            x: clientX - this.previousMousePosition.x,
            y: clientY - this.previousMousePosition.y
        };

        this.targetRotation.y += deltaMove.x * 0.01;
        this.targetRotation.x += deltaMove.y * 0.01;
        
        // Clamp rotation
        this.targetRotation.x = Math.max(-Math.PI / 3, Math.min(Math.PI / 3, this.targetRotation.x));

        this.previousMousePosition = { x: clientX, y: clientY };
    }

    onPointerUp() {
        this.isDragging = false;
        document.getElementById('threejs-container').style.cursor = 'grab';
    }

    onWheel(event) {
        event.preventDefault();
        const scale = 1 + event.deltaY * 0.001;
        this.camera.position.multiplyScalar(scale);
        
        const distance = this.camera.position.length();
        if (distance < 3) {
            this.camera.position.normalize().multiplyScalar(3);
        } else if (distance > 10) {
            this.camera.position.normalize().multiplyScalar(10);
        }
    }

    animate() {
        this.animationId = requestAnimationFrame(this.animate.bind(this));
        
        if (this.headband && this.renderer && this.camera) {
            // Smooth rotation
            this.rotation.x += (this.targetRotation.x - this.rotation.x) * 0.1;
            this.rotation.y += (this.targetRotation.y - this.rotation.y) * 0.1;
            
            this.headband.rotation.x = this.rotation.x;
            this.headband.rotation.y = this.rotation.y;
            
            // Enhanced sensor animations
            const time = Date.now() * 0.002;
            let sensorIndex = 0;
            
            this.headband.children.forEach((child, index) => {
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
                if (child.geometry && child.geometry.type === 'RingGeometry' && child.material.opacity !== undefined) {
                    const glowPulse = (Math.sin(time * 1.5 + index * 0.8) + 1) * 0.3 + 0.1;
                    child.material.opacity = glowPulse;
                    child.rotation.z += 0.005;
                }
            });
            
            // Animate status LEDs
            const ledElements = this.headband.children.filter(child => 
                child.geometry && child.geometry.type === 'SphereGeometry' && child.position.x > 2
            );
            
            ledElements.forEach((led, i) => {
                const ledTime = Date.now() * (0.003 + i * 0.001);
                const brightness = (Math.sin(ledTime) + 1) * 0.5;
                led.material.opacity = 0.4 + brightness * 0.6;
            });
            
            // Subtle floating
            if (!this.isDragging) {
                this.headband.position.y = Math.sin(time * 0.5) * 0.1;
            }
            
            this.camera.lookAt(this.headband.position);
            this.renderer.render(this.scene, this.camera);
        }
    }

    showFallbackModel() {
        const container = document.getElementById('threejs-container');
        container.innerHTML = `
            <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--primary-color);">
                <div style="font-size: 4rem; margin-bottom: 1rem;">🎧</div>
                <div style="font-size: 1.5rem; text-align: center; font-weight: 600;">
                    KAVLO Smart Headband
                </div>
                <div style="font-size: 1rem; opacity: 0.7; margin-top: 0.5rem;">
                    Advanced Health Monitoring
                </div>
            </div>
        `;
    }

    // Enhanced FAQ Database
    faqDatabase = {
        "what is kavlo": {
            answer: "KAVLO is an innovative smart headband designed to track your health in real time during workouts and physical activities. It features 5 advanced sensors including Heart Rate (PPG), Skin Temperature, Galvanic Skin Response (GSR), Accelerometer & Gyroscope, and Pulse Oximeter (SpO₂)."
        },
        "how does it work": {
            answer: "KAVLO uses multiple sensors to monitor your vital signs and movement patterns. The AI-powered system analyzes this data in real-time to detect signs of overexertion and provides gentle vibration alerts when it's time to slow down or stop exercising."
        },
        "what sensors": {
            answer: "KAVLO includes 5 key sensors:\n• Heart Rate (PPG) sensor\n• Skin Temperature sensor\n• Galvanic Skin Response (GSR)\n• Accelerometer & Gyroscope\n• Pulse Oximeter (SpO₂)\n\nThese work together to provide comprehensive health monitoring."
        },
        "battery life": {
            answer: "KAVLO offers 12+ hours of continuous monitoring with ultra-efficient power management. It supports fast wireless charging that gets you back to 100% in just 2 hours."
        },
        "weight": {
            answer: "KAVLO weighs only 45 grams, making it comfortable for extended wear during any physical activity."
        },
        "price cost": {
            answer: "KAVLO is available for $99. This includes the smart headband device and access to the companion mobile app with all premium features."
        },
        "mobile app": {
            answer: "The KAVLO mobile app is available for Android and iOS devices. It features an inclusive UI/UX design, real-time dashboards, personalized recommendations, and secure data sharing with trainers, doctors, or caregivers."
        },
        "safety alerts": {
            answer: "KAVLO uses gentle vibration notifications to alert users when it's time to slow down or stop exercising. This makes it highly accessible for people with disabilities, pregnant women, beginners, and anyone wanting safer workouts."
        },
        "posture monitoring": {
            answer: "Yes! KAVLO's built-in accelerometer and gyroscope detect unstable movements or poor posture, helping reduce injury risk. This is especially beneficial for users with neurological or physical conditions."
        },
        "ai features": {
            answer: "KAVLO's AI-powered fatigue analyzer uses machine learning to detect patterns in your biometric data that indicate overexertion. It learns your personal limits and provides increasingly accurate alerts over time."
        }
    };

    quickQuestions = [
        "What is KAVLO?",
        "How does it work?", 
        "What sensors does it have?",
        "Battery life?",
        "How much does it cost?",
        "Is there a mobile app?"
    ];

    initChatbot() {
        const chatbotButton = document.getElementById('chatbotButton');
        const chatbotWindow = document.getElementById('chatbotWindow');
        const closeBtn = chatbotWindow?.querySelector('.close-btn');
        const sendBtn = chatbotWindow?.querySelector('.send-btn');
        const input = chatbotWindow?.querySelector('input');

        chatbotButton?.addEventListener('click', this.toggleChatbot.bind(this));
        chatbotButton?.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.toggleChatbot();
            }
        });
        
        closeBtn?.addEventListener('click', this.toggleChatbot.bind(this));
        sendBtn?.addEventListener('click', this.sendMessage.bind(this));
        
        input?.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                this.sendMessage();
            }
        });
    }

    toggleChatbot() {
        const chatbotWindow = document.getElementById('chatbotWindow');
        this.chatbotOpen = !this.chatbotOpen;
        
        if (this.chatbotOpen) {
            chatbotWindow.classList.add('active');
            chatbotWindow.querySelector('input')?.focus();
            this.generateQuickQuestions();
        } else {
            chatbotWindow.classList.remove('active');
        }
    }

    generateQuickQuestions() {
        let quickQuestionsContainer = document.querySelector('.quick-questions');
        if (!quickQuestionsContainer) {
            const chatbotWindow = document.getElementById('chatbotWindow');
            const inputSection = chatbotWindow.querySelector('.chatbot-input');
            
            quickQuestionsContainer = document.createElement('div');
            quickQuestionsContainer.className = 'quick-questions';
            quickQuestionsContainer.style.cssText = `
                padding: 1rem;
                display: flex;
                flex-wrap: wrap;
                gap: 0.5rem;
                border-top: 1px solid rgba(0, 255, 136, 0.2);
            `;
            chatbotWindow.insertBefore(quickQuestionsContainer, inputSection);
        }
        
        quickQuestionsContainer.innerHTML = '';
        
        this.quickQuestions.forEach(question => {
            const questionBtn = document.createElement('div');
            questionBtn.className = 'quick-question';
            questionBtn.textContent = question;
            questionBtn.style.cssText = `
                background: rgba(0, 255, 136, 0.1);
                border: 1px solid rgba(0, 255, 136, 0.3);
                border-radius: 15px;
                padding: 0.5rem 1rem;
                cursor: pointer;
                font-size: 0.85rem;
                color: var(--accent-color);
                transition: all 0.3s ease;
            `;
            questionBtn.onmouseenter = () => {
                questionBtn.style.background = 'rgba(0, 255, 136, 0.2)';
            };
            questionBtn.onmouseleave = () => {
                questionBtn.style.background = 'rgba(0, 255, 136, 0.1)';
            };
            questionBtn.onclick = () => this.handleQuickQuestion(question);
            quickQuestionsContainer.appendChild(questionBtn);
        });
    }

    handleQuickQuestion(question) {
        const input = document.querySelector('.chatbot-input input');
        input.value = question;
        this.sendMessage();
    }

    sendMessage() {
        const input = document.querySelector('.chatbot-input input');
        const message = input.value.trim();
        
        if (!message) return;
        
        this.addMessage(message, true);
        input.value = '';
        
        // Show typing indicator
        this.showTypingIndicator();
        
        // Simulate response delay
        setTimeout(() => {
            this.hideTypingIndicator();
            const response = this.generateResponse(message);
            this.addMessage(response, false);
        }, 1000 + Math.random() * 1000);
    }

    addMessage(content, isUser = false) {
        const messagesContainer = document.getElementById('chatbotMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isUser ? 'user-message' : 'bot-message'}`;
        
        const messageContent = document.createElement('div');
        messageContent.className = 'message-content';
        messageContent.innerHTML = content.replace(/\n/g, '<br>');
        
        messageDiv.appendChild(messageContent);
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    showTypingIndicator() {
        if (this.isTyping) return;
        
        this.isTyping = true;
        const messagesContainer = document.getElementById('chatbotMessages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message bot-message';
        typingDiv.id = 'typing-indicator';
        
        const typingContent = document.createElement('div');
        typingContent.className = 'typing-indicator';
        typingContent.style.cssText = `
            display: flex;
            align-items: center;
            gap: 0.5rem;
            padding: 1rem 1.25rem;
            background: linear-gradient(135deg, rgba(0, 255, 136, 0.15), rgba(0, 204, 255, 0.15));
            border: 1px solid var(--border-glow);
            border-radius: 18px;
            font-size: 0.95rem;
            color: var(--accent-color);
        `;
        typingContent.innerHTML = `
            <span>KAVLO Assistant is typing</span>
            <div class="typing-dots" style="display: flex; gap: 3px;">
                <div class="typing-dot" style="width: 4px; height: 4px; background: var(--primary-color); border-radius: 50%; animation: typing 1.5s ease-in-out infinite;"></div>
                <div class="typing-dot" style="width: 4px; height: 4px; background: var(--primary-color); border-radius: 50%; animation: typing 1.5s ease-in-out 0.2s infinite;"></div>
                <div class="typing-dot" style="width: 4px; height: 4px; background: var(--primary-color); border-radius: 50%; animation: typing 1.5s ease-in-out 0.4s infinite;"></div>
            </div>
        `;
        
        // Add typing animation CSS if not exists
        if (!document.querySelector('#typingStyle')) {
            const style = document.createElement('style');
            style.id = 'typingStyle';
            style.textContent = `
                @keyframes typing {
                    0%, 80%, 100% {
                        transform: translateY(0);
                        opacity: 0.5;
                    }
                    40% {
                        transform: translateY(-3px);
                        opacity: 1;
                    }
                }
            `;
            document.head.appendChild(style);
        }
        
        typingDiv.appendChild(typingContent);
        messagesContainer.appendChild(typingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    hideTypingIndicator() {
        const typingIndicator = document.getElementById('typing-indicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
        this.isTyping = false;
    }

    findBestMatch(userInput) {
        const input = userInput.toLowerCase();
        let bestMatch = null;
        let highestScore = 0;
        
        for (const [key, data] of Object.entries(this.faqDatabase)) {
            const keywords = key.split(' ');
            let score = 0;
            
            keywords.forEach(keyword => {
                if (input.includes(keyword)) {
                    score += keyword.length;
                }
            });
            
            // Bonus for exact phrase matches
            if (input.includes(key)) {
                score += key.length * 2;
            }
            
            if (score > highestScore) {
                highestScore = score;
                bestMatch = data;
            }
        }
        
        return highestScore > 3 ? bestMatch : null;
    }

    generateResponse(userInput) {
        const match = this.findBestMatch(userInput);
        
        if (match) {
            return match.answer;
        }
        
        // Default responses for common greetings and unmatched queries
        const input = userInput.toLowerCase();
        
        if (input.includes('hello') || input.includes('hi') || input.includes('hey')) {
            return "Hello! I'm here to help you learn about KAVLO. You can ask me about features, specifications, pricing, or how the device works. What would you like to know?";
        }
        
        if (input.includes('thanks') || input.includes('thank you')) {
            return "You're welcome! Is there anything else you'd like to know about KAVLO?";
        }
        
        if (input.includes('bye') || input.includes('goodbye')) {
            return "Thanks for your interest in KAVLO! Feel free to reach out anytime if you have more questions. Have a great workout!";
        }
        
        // Default fallback
        return `I'd be happy to help you learn about KAVLO! I can provide information about:

• Product features and specifications
• How the sensors and AI work
• Pricing and availability  
• Mobile app features
• Safety and accessibility features
• Battery life and technical specs

Try asking something like "What sensors does KAVLO have?" or click one of the quick questions above!`;
    }

    initScrollEffects() {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, observerOptions);

        // Observe sections for scroll animations
        document.querySelectorAll('.feature-card, .spec-item').forEach(el => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            el.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
            observer.observe(el);
        });
    }

    initAccessibility() {
        // Skip link for keyboard navigation
        const skipLink = document.createElement('a');
        skipLink.href = '#main-content';
        skipLink.textContent = 'Skip to main content';
        skipLink.style.cssText = `
            position: absolute;
            top: -40px;
            left: 6px;
            background: var(--primary-color);
            color: black;
            padding: 8px;
            text-decoration: none;
            z-index: 10001;
            font-weight: 600;
            transition: top 0.3s;
        `;
        skipLink.addEventListener('focus', () => {
            skipLink.style.top = '6px';
        });
        skipLink.addEventListener('blur', () => {
            skipLink.style.top = '-40px';
        });
        document.body.prepend(skipLink);

        // Add main content landmark
        const hero = document.getElementById('home');
        if (hero) {
            hero.setAttribute('id', 'main-content');
            hero.setAttribute('tabindex', '-1');
        }
    }

    addInteractionFeedback() {
        // Add click ripple effects to buttons
        const buttons = document.querySelectorAll('.btn, .feature-card, .app-download');
        
        buttons.forEach(button => {
            button.addEventListener('click', (e) => {
                const ripple = document.createElement('div');
                const rect = button.getBoundingClientRect();
                const size = Math.max(rect.width, rect.height);
                const x = e.clientX - rect.left - size / 2;
                const y = e.clientY - rect.top - size / 2;
                
                ripple.style.cssText = `
                    position: absolute;
                    width: ${size}px;
                    height: ${size}px;
                    left: ${x}px;
                    top: ${y}px;
                    background: radial-gradient(circle, rgba(0, 255, 136, 0.3) 0%, transparent 70%);
                    border-radius: 50%;
                    pointer-events: none;
                    animation: ripple 0.6s ease-out;
                    z-index: 0;
                `;
                
                button.style.position = 'relative';
                button.style.overflow = 'hidden';
                button.appendChild(ripple);
                
                setTimeout(() => {
                    if (ripple.parentNode) {
                        ripple.remove();
                    }
                }, 600);
            });
        });
        
        // Add ripple animation if not exists
        if (!document.querySelector('#rippleStyle')) {
            const style = document.createElement('style');
            style.id = 'rippleStyle';
            style.textContent = `
                @keyframes ripple {
                    0% {
                        transform: scale(0);
                        opacity: 1;
                    }
                    100% {
                        transform: scale(2);
                        opacity: 0;
                    }
                }
            `;
            document.head.appendChild(style);
        }
    }

    // Cleanup method
    destroy() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
        }
        
        if (this.renderer) {
            this.renderer.dispose();
        }
    }
}

// Global functions
function scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function downloadApp(platform) {
    const messages = {
        ios: "Redirecting to App Store...\n\nKAVLO for iOS\nVersion 2.1.0\nCompatible with iOS 15.0+\nSize: 127 MB",
        android: "Redirecting to Google Play Store...\n\nKAVLO for Android\nVersion 2.1.2\nCompatible with Android 8.0+\nSize: 95 MB"
    };
    
    alert(messages[platform]);
    // In a real implementation, this would redirect to app stores
}

// Initialize website
let kavloSite;

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        kavloSite = new KavloWebsite();
    });
} else {
    kavloSite = new KavloWebsite();
}

// Handle page unload
window.addEventListener('beforeunload', () => {
    kavloSite?.destroy();
});

// Handle resize with debouncing
let resizeTimeout;
window.addEventListener('resize', () => {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(() => {
        if (kavloSite?.camera && kavloSite?.renderer) {
            const container = document.getElementById('threejs-container');
            if (container) {
                kavloSite.camera.aspect = container.offsetWidth / container.offsetHeight;
                kavloSite.camera.updateProjectionMatrix();
                kavloSite.renderer.setSize(container.offsetWidth, container.offsetHeight);
            }
        }
    }, 250);
}, { passive: true });

// Enhanced error handling
window.addEventListener('error', function(e) {
    console.warn('KAVLO: Minor error occurred, but site continues to function:', e.message);
});

// Performance monitoring
window.addEventListener('load', () => {
    if (performance.navigation) {
        const loadTime = performance.navigation.loadEventEnd - performance.navigation.fetchStart;
        console.log(`KAVLO site fully loaded in ${loadTime}ms`);
    }
});