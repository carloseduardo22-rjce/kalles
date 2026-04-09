/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },
  allowedDevOrigins: [
    "3ffc-2804-1494-dbb-aa00-a54d-cb78-3de9-27c6.ngrok-free.app",
  ],
  async rewrites() {
    return {
      // afterFiles ensures filesystem routes (Route Handlers in app/api/)
      // are checked FIRST. Only unmatched /api/* paths fall through here.
      afterFiles: [
        {
          source: "/api/:path*",
          destination: "http://localhost:8080/api/:path*",
        },
      ],
    };
  },
};

export default nextConfig;
