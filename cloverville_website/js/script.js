/* W3Schools function */
function myFunction() {
  var x = document.getElementById("myTopnav");
  if (x.className === "main-nav") {
    x.className += " responsive";
  } else {
    x.className = "main-nav";
  }
}

async function loadGreenActions() {
  try {
    const response = await fetch("cloverville_export.json");
    const data = await response.json();

    const grid = document.getElementById("greenGrid");
    const totalPointsElement = document.getElementById("totalGreenPoints");
    const communityPoolValue = document.getElementById("communityPoolValue");

    grid.innerHTML = ""; 
    let totalPoints = 0;

    data.greenActions.forEach(action => {
      totalPoints += action.greenPoints;

      const card = document.createElement("article");
      card.classList.add("green-card");
      card.innerHTML = `
        <h2>${action.name}</h2>
        <div class="green-points">+${action.greenPoints} points</div>
        <p>${action.description || ""}</p>
      `;
      grid.appendChild(card);
    });

    totalPointsElement.textContent = totalPoints + " points";
    if (data.communityPool !== undefined) {
      communityPoolValue.textContent = data.communityPool;
    }

    updateRewards(totalPoints);

  } catch (error) {
    console.error("Error loading data:", error);
  }
}

function updateRewards(currentPoints) {
  const rewardItems = document.querySelectorAll('.reward-item');
  rewardItems.forEach(item => {
    const threshold = parseInt(item.getAttribute('data-threshold'));
    const icon = item.querySelector('.reward-icon');

    if (currentPoints >= threshold) {
      item.classList.remove('locked');
      item.classList.add('unlocked');
      icon.textContent = "🔓";
    } else {
      item.classList.remove('unlocked');
      item.classList.add('locked');
      icon.textContent = "🔒";
    }
  });
}

loadGreenActions();
