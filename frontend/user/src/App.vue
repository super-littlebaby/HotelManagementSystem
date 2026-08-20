<script setup>
import Header from './components/Header.vue'
</script>

<template>
  <div class="app-container">
    <Header />
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <div class="container">
        <p>&copy; 2026 Hotel Management System. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>


