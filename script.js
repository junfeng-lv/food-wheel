const wheel = document.getElementById("wheel");
const ctx = wheel.getContext("2d");

const optionsInput = document.getElementById("optionsInput");
const winnerText = document.getElementById("winnerText");
const winnerBox = document.getElementById("winnerBox");
const entryCount = document.getElementById("entryCount");
const confetti = document.getElementById("confetti");

const spinBtn = document.getElementById("spinBtn");
const sampleBtn = document.getElementById("sampleBtn");
const shuffleBtn = document.getElementById("shuffleBtn");
const clearBtn = document.getElementById("clearBtn");
const clearTopBtn = document.getElementById("clearTopBtn");

const colors = [
  "#ef4444",
  "#f97316",
  "#eab308",
  "#22c55e",
  "#14b8a6",
  "#3b82f6",
  "#8b5cf6",
  "#ec4899"
];

let options = [];
let currentRotation = 0;
let isSpinning = false;

function getOptions() {
  return optionsInput.value
    .split("\n")
    .map(item => item.trim())
    .filter(item => item !== "");
}

function updateEntryCount() {
  const currentOptions = getOptions();
  entryCount.textContent = currentOptions.length;
}

function drawWheel() {
  options = getOptions();
  updateEntryCount();

  const displayOptions = options.length > 0 ? options : ["Add", "Entries"];
  const centerX = wheel.width / 2;
  const centerY = wheel.height / 2;
  const radius = wheel.width / 2 - 8;
  const sliceAngle = (Math.PI * 2) / displayOptions.length;

  ctx.clearRect(0, 0, wheel.width, wheel.height);

  displayOptions.forEach((option, index) => {
    const startAngle = index * sliceAngle + currentRotation;
    const endAngle = startAngle + sliceAngle;

    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.arc(centerX, centerY, radius, startAngle, endAngle);
    ctx.closePath();
    ctx.fillStyle = colors[index % colors.length];
    ctx.fill();

    ctx.strokeStyle = "#ffffff";
    ctx.lineWidth = 4;
    ctx.stroke();

    ctx.save();
    ctx.translate(centerX, centerY);
    ctx.rotate(startAngle + sliceAngle / 2);
    ctx.textAlign = "right";
    ctx.fillStyle = "#ffffff";
    ctx.font = "bold 22px Arial";

    const displayText = option.length > 14 ? option.slice(0, 14) + "..." : option;
    ctx.fillText(displayText, radius - 28, 8);

    ctx.restore();
  });

  ctx.beginPath();
  ctx.arc(centerX, centerY, 66, 0, Math.PI * 2);
  ctx.fillStyle = "#ffffff";
  ctx.fill();
}

function spinWheel() {
  if (isSpinning) {
    return;
  }

  options = getOptions();

  if (options.length < 2) {
    winnerBox.classList.remove("celebrate");
    winnerText.textContent = "Add at least 2 entries.";
    return;
  }

  isSpinning = true;
  spinBtn.disabled = true;
  winnerBox.classList.remove("celebrate");
  winnerText.textContent = "Spinning...";

  const randomIndex = Math.floor(Math.random() * options.length);
  const sliceAngle = (Math.PI * 2) / options.length;

  const selectedSliceCenter = randomIndex * sliceAngle + sliceAngle / 2;
  const pointerPosition = Math.PI * 1.5;

  let finalRotation = pointerPosition - selectedSliceCenter;

  while (finalRotation < currentRotation) {
    finalRotation += Math.PI * 2;
  }

  finalRotation += Math.PI * 2 * 6;

  const startRotation = currentRotation;
  const rotationChange = finalRotation - startRotation;
  const duration = 3800;
  const startTime = performance.now();

  function animateSpin(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const easeOut = 1 - Math.pow(1 - progress, 4);

    currentRotation = startRotation + rotationChange * easeOut;
    drawWheel();

    if (progress < 1) {
      requestAnimationFrame(animateSpin);
    } else {
      currentRotation = finalRotation % (Math.PI * 2);
      drawWheel();

      winnerBox.classList.add("celebrate");
      winnerText.textContent = `🎉 ${options[randomIndex]} 🎉`;
      launchConfetti();

      isSpinning = false;
      spinBtn.disabled = false;
    }
  }

  requestAnimationFrame(animateSpin);
}

function useSampleOptions() {
  optionsInput.value = "Sophia\nLiam\nEmma\nNoah\nOlivia\nJames\nAva\nWilliam";
  winnerBox.classList.remove("celebrate");
  winnerText.textContent = "Ready to spin!";
  currentRotation = 0;
  drawWheel();
}

function clearOptions() {
  optionsInput.value = "";
  winnerBox.classList.remove("celebrate");
  winnerText.textContent = "Add entries and spin the wheel";
  currentRotation = 0;
  drawWheel();
}

function shuffleOptions() {
  const currentOptions = getOptions();

  if (currentOptions.length < 2) {
    winnerText.textContent = "Add at least 2 entries to shuffle.";
    return;
  }

  for (let i = currentOptions.length - 1; i > 0; i--) {
    const randomPosition = Math.floor(Math.random() * (i + 1));
    [currentOptions[i], currentOptions[randomPosition]] = [
      currentOptions[randomPosition],
      currentOptions[i]
    ];
  }

  optionsInput.value = currentOptions.join("\n");
  winnerBox.classList.remove("celebrate");
  winnerText.textContent = "Entries shuffled!";
  currentRotation = 0;
  drawWheel();
}

function launchConfetti() {
  confetti.innerHTML = "";

  const confettiColors = ["#ef4444", "#f97316", "#eab308", "#22c55e", "#3b82f6", "#8b5cf6", "#ec4899"];

  for (let i = 0; i < 80; i++) {
    const piece = document.createElement("span");

    piece.style.left = Math.random() * 100 + "vw";
    piece.style.backgroundColor = confettiColors[Math.floor(Math.random() * confettiColors.length)];
    piece.style.animationDelay = Math.random() * 0.5 + "s";

    confetti.appendChild(piece);
  }

  setTimeout(() => {
    confetti.innerHTML = "";
  }, 3200);
}

optionsInput.addEventListener("input", () => {
  winnerBox.classList.remove("celebrate");
  winnerText.textContent = "Ready to spin!";
  drawWheel();
});

spinBtn.addEventListener("click", spinWheel);
sampleBtn.addEventListener("click", useSampleOptions);
shuffleBtn.addEventListener("click", shuffleOptions);
clearBtn.addEventListener("click", clearOptions);
clearTopBtn.addEventListener("click", clearOptions);

useSampleOptions();