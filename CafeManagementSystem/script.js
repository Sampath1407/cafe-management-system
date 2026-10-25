
const categoryButtons = document.querySelectorAll("[data-category]");
const cartContainer = document.getElementById("cart-items");
const cartTotal = document.getElementById("cart-total");
const checkoutButton = document.getElementById("checkout-button");
const paymentMessage = document.getElementById("payment-message");

const menuGrid = document.querySelector(".menu-grid");
const cart = {};

function formatPrice(amount) {
    return "₹" + Number(amount).toFixed(2);
}

// Load menu items from Java
async function loadMenu() {
    try {
        const response = await fetch("http://localhost:8080/api/menu");

        if (!response.ok) {
            throw new Error("Could not load the menu.");
        }

        const menuItems = await response.json();

        if (!menuGrid) {
            throw new Error("Menu container not found.");
        }

        renderMenu(menuItems);
        console.log("Menu loaded from Java successfully!");
    } catch (error) {
        console.error("Menu loading failed:", error);
        paymentMessage.textContent =
            "Unable to load the menu. Please make sure the Java server is running.";
    }
}

// Build menu cards using the existing card design
function renderMenu(items) {
    const template = menuGrid.querySelector(".menu-card");

    if (!template) {
        console.error("No menu card template found in the HTML.");
        return;
    }

    menuGrid.replaceChildren();

    items.forEach(item => {
        const card = template.cloneNode(true);

        card.dataset.name = item.name;
        card.dataset.price = item.price;
        card.dataset.category = item.category;
        card.hidden = false;

        const title = card.querySelector("h3");
        if (title) {
            title.textContent = item.name;
        }

        const addButton = card.querySelector("[data-add]");
        if (addButton) {
            addButton.dataset.add = item.name;
        }

        const categoryText = card.querySelector(".menu-category, .category");
        if (categoryText) {
            categoryText.textContent = item.category;
        }

        const priceText = card.querySelector(".menu-price, .price");
        if (priceText) {
            priceText.textContent = formatPrice(item.price);
        } else {
            const textElements = card.querySelectorAll("p, span, strong");
            for (const element of textElements) {
                if (/₹|rs\.?/i.test(element.textContent.trim())) {
                    element.textContent = formatPrice(item.price);
                    break;
                }
            }
        }

        menuGrid.appendChild(card);
    });

    applyCategoryFilter("All");
}

// Filter menu items by category
function applyCategoryFilter(category) {
    categoryButtons.forEach(button => {
        button.classList.toggle(
            "active",
            button.dataset.category === category
        );
    });

    document.querySelectorAll(".menu-card").forEach(card => {
        card.hidden = category !== "All" &&
            card.dataset.category !== category;
    });
}

categoryButtons.forEach(button => {
    button.addEventListener("click", () => {
        applyCategoryFilter(button.dataset.category);
    });
});

// Add an item to the cart
document.addEventListener("click", event => {
    const button = event.target.closest("[data-add]");

    if (!button) {
        return;
    }

    const card = button.closest(".menu-card");

    if (!card) {
        return;
    }

    const name = card.dataset.name;
    const price = Number(card.dataset.price);

    if (!cart[name]) {
        cart[name] = {
            price: price,
            quantity: 0
        };
    }

    cart[name].quantity++;
    paymentMessage.textContent = "";

    renderCart();
});

// Increase or decrease an item's quantity
function changeQuantity(name, amount) {
    if (!cart[name]) {
        return;
    }

    cart[name].quantity += amount;

    if (cart[name].quantity <= 0) {
        delete cart[name];
    }

    paymentMessage.textContent = "";
    renderCart();
}

// Draw the cart and calculate its total
function renderCart() {
    cartContainer.replaceChildren();

    const names = Object.keys(cart);
    let total = 0;

    if (names.length === 0) {
        const emptyMessage = document.createElement("p");
        emptyMessage.className = "empty-cart";
        emptyMessage.textContent =
            "Your cart is empty. Add something yummy!";
        cartContainer.appendChild(emptyMessage);
    }

    names.forEach(name => {
        const item = cart[name];
        const subtotal = item.price * item.quantity;

        total += subtotal;

        const row = document.createElement("div");
        row.className = "cart-item";

        const details = document.createElement("div");

        const title = document.createElement("h3");
        title.textContent = name;

        const price = document.createElement("p");
        price.textContent = formatPrice(item.price) + " each";

        details.append(title, price);

        const controls = document.createElement("div");
        controls.className = "quantity-controls";

        const decrease = document.createElement("button");
        decrease.type = "button";
        decrease.textContent = "−";
        decrease.setAttribute("aria-label", "Decrease " + name);
        decrease.addEventListener("click", () => {
            changeQuantity(name, -1);
        });

        const quantity = document.createElement("span");
        quantity.textContent = item.quantity;

        const increase = document.createElement("button");
        increase.type = "button";
        increase.textContent = "+";
        increase.setAttribute("aria-label", "Increase " + name);
        increase.addEventListener("click", () => {
            changeQuantity(name, 1);
        });

        controls.append(decrease, quantity, increase);

        const amount = document.createElement("strong");
        amount.textContent = formatPrice(subtotal);

        row.append(details, controls, amount);
        cartContainer.appendChild(row);
    });

    cartTotal.textContent = formatPrice(total);
    checkoutButton.disabled = names.length === 0;
}

// Demo checkout for now; Java order processing comes next

checkoutButton.addEventListener("click", async () => {
    const names = Object.keys(cart);

    if (names.length === 0) {
        return;
    }

    const confirmed = window.confirm(
        "Confirm cash payment for your order?"
    );

    if (!confirmed) {
        return;
    }

    checkoutButton.disabled = true;
    paymentMessage.textContent = "Placing your order...";

    try {
        const orderItems = names.map(name => ({
            name: name,
            quantity: cart[name].quantity
        }));

        const response = await fetch(
            "http://localhost:8080/api/checkout",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    paymentMethod: "Cash",
                    items: orderItems
                })
            }
        );

        const result = await response.json();

        if (!response.ok) {
            throw new Error(
                result.error || "Could not place your order."
            );
        }

        names.forEach(name => delete cart[name]);

        renderCart();

        paymentMessage.textContent =
            "Order #" + result.orderId +
            " confirmed! Total: " + formatPrice(result.total) +
            ". Please pay cash at the counter.";

    } catch (error) {
        paymentMessage.textContent =
            "Checkout failed: " + error.message +
            ". Please check that the Java server is running.";
    } finally {
        checkoutButton.disabled = Object.keys(cart).length === 0;
    }
});

// Start the menu connection
renderCart();
loadMenu();
