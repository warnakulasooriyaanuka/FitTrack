/* FitTrack Coach: a chibi 3D character built from primitives (no external model files).
   Public API (used by the Android app through WebView.evaluateJavascript):
     coach.setTalking(bool)  mouth moves + head nods
     coach.wave()            waves the right arm
     coach.cheer()           jumps with both arms up
   Drag to turn the character; tap to make it wave. */
(function (global) {
  'use strict';
  var LIME = 0xA5FF00;

  function build(THREE) {
    var root = new THREE.Group();

    var gradient = new THREE.DataTexture(new Uint8Array([125, 195, 255]), 3, 1, THREE.RedFormat);
    gradient.minFilter = THREE.NearestFilter;
    gradient.magFilter = THREE.NearestFilter;
    gradient.needsUpdate = true;
    var outlineMat = new THREE.MeshBasicMaterial({ color: 0x050505, side: THREE.BackSide });

    function toon(color) { return new THREE.MeshToonMaterial({ color: color, gradientMap: gradient }); }
    function solid(color) { return new THREE.MeshBasicMaterial({ color: color }); }
    /** toon mesh with an inverted-hull outline (cel-shaded look) */
    function part(geo, color, outline) {
      var m = new THREE.Mesh(geo, toon(color));
      if (outline !== 0) {
        var o = new THREE.Mesh(geo, outlineMat);
        o.scale.setScalar(1 + (outline || 0.045));
        m.add(o);
      }
      return m;
    }

    var SKIN = 0xF6CBA9, HAIR = 0x34343f, SHORTS = 0x17171c;

    /* ---------- head ---------- */
    var head = new THREE.Group();
    head.position.set(0, 2.15, 0);
    root.add(head);

    var skull = part(new THREE.SphereGeometry(1, 40, 28), SKIN);
    skull.scale.set(1.06, 0.96, 1);
    head.add(skull);

    [-1, 1].forEach(function (s) {
      var ear = part(new THREE.SphereGeometry(0.17, 16, 12), SKIN, 0.06);
      ear.position.set(s * 1.04, -0.08, -0.02);
      ear.scale.set(0.6, 1, 0.8);
      head.add(ear);
    });

    // hair cap, tilted back so the forehead shows
    var hairCap = part(new THREE.SphereGeometry(1.09, 40, 28, 0, Math.PI * 2, 0, Math.PI * 0.49), HAIR, 0.035);
    hairCap.position.y = 0.04;
    hairCap.rotation.x = -0.3;
    head.add(hairCap);

    // bangs
    for (var i = -2; i <= 2; i++) {
      var bang = part(new THREE.ConeGeometry(0.29, 0.58, 6), HAIR, 0.05);
      var a = Math.abs(i);
      bang.position.set(i * 0.34, 0.62 - a * 0.05, 0.78 - a * 0.16);
      bang.rotation.set(-0.32, i * 0.18, Math.PI + i * 0.1);
      head.add(bang);
    }
    // top tufts
    [[-0.38, 1.1, 0.05, 0.45], [0, 1.2, -0.05, 0], [0.38, 1.08, 0.05, -0.45]].forEach(function (t) {
      var tuft = part(new THREE.ConeGeometry(0.26, 0.72, 6), HAIR, 0.05);
      tuft.position.set(t[0], t[1], t[2]);
      tuft.rotation.z = t[3];
      head.add(tuft);
    });

    // lime headband
    var band = part(new THREE.TorusGeometry(0.98, 0.075, 10, 56), LIME, 0.06);
    band.rotation.x = Math.PI / 2 - 0.12;
    band.position.y = 0.36;
    band.scale.set(1.06, 1, 1);
    head.add(band);

    /* ---------- face ---------- */
    var eyes = [];
    [-1, 1].forEach(function (s) {
      var eye = new THREE.Group();
      eye.position.set(s * 0.4, -0.1, 0.92);
      var iris = new THREE.Mesh(new THREE.SphereGeometry(1, 20, 16), solid(0x121218));
      iris.scale.set(0.16, 0.22, 0.08);
      eye.add(iris);
      var shine = new THREE.Mesh(new THREE.SphereGeometry(1, 12, 10), solid(0xffffff));
      shine.scale.set(0.055, 0.075, 0.03);
      shine.position.set(s * -0.05, 0.08, 0.075);
      eye.add(shine);
      var shine2 = new THREE.Mesh(new THREE.SphereGeometry(1, 10, 8), solid(0xffffff));
      shine2.scale.set(0.028, 0.035, 0.02);
      shine2.position.set(s * 0.06, -0.07, 0.075);
      eye.add(shine2);
      head.add(eye);
      eyes.push(eye);
    });
    [-1, 1].forEach(function (s) {
      var brow = new THREE.Mesh(new THREE.CapsuleGeometry(0.018, 0.16, 4, 8), solid(0x1b1b22));
      brow.position.set(s * 0.4, 0.2, 0.93);
      brow.rotation.z = Math.PI / 2 + s * 0.12;
      head.add(brow);
    });
    [-1, 1].forEach(function (s) {
      var blush = new THREE.Mesh(new THREE.SphereGeometry(1, 16, 10), new THREE.MeshBasicMaterial({ color: 0xff8fa8, transparent: true, opacity: 0.75 }));
      blush.scale.set(0.17, 0.09, 0.03);
      blush.position.set(s * 0.64, -0.3, 0.74);
      blush.rotation.y = s * 0.65;
      head.add(blush);
    });
    var smile = new THREE.Mesh(new THREE.TorusGeometry(0.12, 0.02, 6, 20, Math.PI), solid(0x3a1218));
    smile.position.set(0, -0.36, 0.93);
    smile.rotation.z = Math.PI;
    head.add(smile);
    var mouth = new THREE.Mesh(new THREE.SphereGeometry(1, 16, 12), solid(0x5b1420));
    mouth.position.set(0, -0.43, 0.9);
    mouth.scale.set(0.12, 0.08, 0.05);
    mouth.visible = false;
    head.add(mouth);

    /* ---------- body ---------- */
    var hoodie = part(new THREE.CapsuleGeometry(0.46, 0.32, 8, 22), LIME, 0.05);
    hoodie.position.y = 1.12;
    root.add(hoodie);
    // black chest stripe
    var stripe = new THREE.Mesh(new THREE.CylinderGeometry(0.472, 0.472, 0.08, 24, 1, true), toon(0x17171c));
    stripe.position.y = 1.0;
    root.add(stripe);
    // drawstrings
    [-1, 1].forEach(function (s) {
      var d = new THREE.Mesh(new THREE.CylinderGeometry(0.012, 0.012, 0.22, 6), solid(0xf4f4f4));
      d.position.set(s * 0.1, 1.2, 0.45);
      root.add(d);
    });
    var shorts = part(new THREE.CylinderGeometry(0.45, 0.42, 0.3, 24), SHORTS, 0.05);
    shorts.position.y = 0.5;
    root.add(shorts);

    [-1, 1].forEach(function (s) {
      var leg = part(new THREE.CapsuleGeometry(0.14, 0.12, 6, 12), SKIN, 0.06);
      leg.position.set(s * 0.2, 0.22, 0);
      root.add(leg);
      var shoe = part(new THREE.SphereGeometry(1, 20, 14), LIME, 0.05);
      shoe.scale.set(0.2, 0.13, 0.32);
      shoe.position.set(s * 0.2, 0.08, 0.1);
      root.add(shoe);
    });

    /* ---------- arms (pivot at the shoulder) ---------- */
    function makeArm(side) {
      var arm = new THREE.Group();
      arm.position.set(side * 0.6, 1.3, 0);
      var sleeve = part(new THREE.CapsuleGeometry(0.13, 0.34, 6, 12), LIME, 0.06);
      sleeve.position.y = -0.24;
      arm.add(sleeve);
      var hand = part(new THREE.SphereGeometry(0.15, 16, 12), SKIN, 0.07);
      hand.position.y = -0.58;
      arm.add(hand);
      root.add(arm);
      return arm;
    }
    var armL = makeArm(-1), armR = makeArm(1);

    // dumbbell in the right hand
    var bell = new THREE.Group();
    bell.position.set(0, -0.58, 0.02);
    bell.scale.setScalar(1.3);
    var handle = new THREE.Mesh(new THREE.CylinderGeometry(0.035, 0.035, 0.42, 10), toon(0xd0d0d6));
    bell.add(handle);
    [-1, 1].forEach(function (s) {
      var w = part(new THREE.CylinderGeometry(0.12, 0.12, 0.09, 18), 0x2a2a32, 0.06);
      w.position.y = s * 0.2;
      bell.add(w);
    });
    armR.add(bell);

    /* ---------- animation state ---------- */
    var st = {
      talking: false, talkAmt: 0,
      waveStart: -10, cheerStart: -10,
      nextBlink: 2.2, blinkStart: -10,
      yawTarget: 0, yaw: 0
    };
    var WAVE_LEN = 1.9, CHEER_LEN = 1.5;

    function update(t) {
      var idle = Math.sin(t * 2.2);
      root.position.y = idle * 0.035;
      hoodie.scale.set(1 + idle * 0.012, 1 - idle * 0.01, 1 + idle * 0.012);

      // head: gentle sway + nod while talking
      st.talkAmt += ((st.talking ? 1 : 0) - st.talkAmt) * 0.2;
      head.rotation.z = Math.sin(t * 1.3) * 0.045;
      head.rotation.x = Math.sin(t * 9) * 0.03 * st.talkAmt + Math.sin(t * 0.9) * 0.015;
      head.rotation.y = Math.sin(t * 0.7) * 0.06;

      // mouth
      var talkingNow = st.talkAmt > 0.08;
      mouth.visible = talkingNow;
      smile.visible = !talkingNow;
      if (talkingNow) mouth.scale.y = 0.03 + 0.1 * Math.abs(Math.sin(t * 13)) * st.talkAmt;

      // blink
      if (t > st.nextBlink) { st.blinkStart = t; st.nextBlink = t + 2.4 + Math.random() * 2.8; }
      var bp = (t - st.blinkStart) / 0.16;
      var open = bp >= 0 && bp <= 1 ? 1 - 0.92 * Math.sin(Math.PI * bp) : 1;
      eyes.forEach(function (e) { e.scale.y = open; });

      // arms: idle sway, wave (right) and cheer (both) raise outward and a little forward so they clear the head
      var armLz = -0.22 + Math.sin(t * 2.2 + 1) * 0.04;
      var armRz = 0.22 - Math.sin(t * 2.2 + 1) * 0.04;
      var raiseL = 0, raiseR = 0, jump = 0;
      var wp = (t - st.waveStart) / WAVE_LEN;
      var cp = (t - st.cheerStart) / CHEER_LEN;
      if (wp >= 0 && wp <= 1) {
        raiseR = wp < 0.12 ? wp / 0.12 : wp > 0.88 ? (1 - wp) / 0.12 : 1;
        armRz += (2.0 + Math.sin(t * 13) * 0.32 - armRz) * raiseR;
      }
      if (cp >= 0 && cp <= 1) {
        var up = cp < 0.1 ? cp / 0.1 : cp > 0.9 ? (1 - cp) / 0.1 : 1;
        raiseL = Math.max(raiseL, up);
        raiseR = Math.max(raiseR, up);
        armLz += (-2.4 + Math.sin(t * 16) * 0.12 - armLz) * up;
        armRz += (2.4 - Math.sin(t * 16) * 0.12 - armRz) * up;
        jump = Math.abs(Math.sin(cp * Math.PI * 3)) * 0.42;
      }
      armL.rotation.z = armLz;
      armR.rotation.z = armRz;
      armL.position.z = 0.45 * raiseL;
      armR.position.z = 0.45 * raiseR;
      root.position.y += jump;

      // drag to turn, springs back
      st.yaw += (st.yawTarget - st.yaw) * 0.14;
      root.rotation.y = st.yaw;
    }

    return {
      root: root,
      update: update,
      setTalking: function (v) { st.talking = !!v; },
      wave: function (t) { st.waveStart = t; },
      cheer: function (t) { st.cheerStart = t; },
      setYawTarget: function (y) { st.yawTarget = y; }
    };
  }

  function start(container) {
    var THREE = global.THREE;
    var renderer;
    try {
      renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
    } catch (e) {
      container.innerHTML = '<div class="fallback">&#127947;</div>';
      return null;
    }
    renderer.setPixelRatio(Math.min(global.devicePixelRatio || 1, 2));
    renderer.setClearColor(0x000000, 0);
    container.appendChild(renderer.domElement);

    var scene = new THREE.Scene();
    var camera = new THREE.PerspectiveCamera(30, 1, 0.1, 60);
    scene.add(new THREE.HemisphereLight(0xffffff, 0x555570, 1.05));
    var key = new THREE.DirectionalLight(0xffffff, 1.05); key.position.set(3, 5, 7); scene.add(key);
    var rim = new THREE.DirectionalLight(LIME, 0.7); rim.position.set(-4, 3, -4); scene.add(rim);

    var c = build(THREE);
    scene.add(c.root);

    function resize() {
      var w = container.clientWidth || 200, h = container.clientHeight || 200;
      renderer.setSize(w, h, false);
      var aspect = w / h;
      camera.aspect = aspect;
      var d = Math.max(9.2, 3.9 / (0.536 * aspect));
      camera.position.set(0, 1.95, d);
      camera.lookAt(0, 1.8, 0);
      camera.updateProjectionMatrix();
    }
    resize();
    global.addEventListener('resize', resize);

    var t0 = Date.now();
    function now() { return (Date.now() - t0) / 1000; }

    var down = false, lastX = 0, moved = 0, yaw = 0;
    container.addEventListener('pointerdown', function (e) { down = true; lastX = e.clientX; moved = 0; });
    container.addEventListener('pointermove', function (e) {
      if (!down) return;
      var dx = e.clientX - lastX; lastX = e.clientX; moved += Math.abs(dx);
      yaw = Math.max(-1.2, Math.min(1.2, yaw + dx * 0.012));
      c.setYawTarget(yaw);
    });
    function release() {
      if (!down) return;
      down = false; yaw = 0; c.setYawTarget(0);
      if (moved < 8) c.wave(now());
    }
    container.addEventListener('pointerup', release);
    container.addEventListener('pointercancel', release);

    function loop() {
      c.update(now());
      renderer.render(scene, camera);
      global.requestAnimationFrame(loop);
    }
    loop();

    var api = {
      setTalking: function (v) { c.setTalking(v); },
      wave: function () { c.wave(now()); },
      cheer: function () { c.cheer(now()); }
    };
    global.coach = api;
    global.setTimeout(function () { api.wave(); }, 700); // say hi
    return api;
  }

  global.FitCoach = { build: build, start: start };
})(typeof window !== 'undefined' ? window : globalThis);
