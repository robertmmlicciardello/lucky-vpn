
#!/bin/bash

echo "🚀 Starting Lucky VPN Master Backend..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating from template..."
    cp .env.example .env
    echo "📝 Please edit .env file with your database credentials"
fi

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Create logs directory
mkdir -p logs

# Start the server
echo "🌟 Starting server on port 3000..."
npm run dev
