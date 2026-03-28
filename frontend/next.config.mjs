/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },
  allowedDevOrigins: [
    "08e8-2804-1494-dbb-aa00-f8ed-896b-5037-a22f.ngrok-free.app",
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
