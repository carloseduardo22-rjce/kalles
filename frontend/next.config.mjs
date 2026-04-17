/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },
  allowedDevOrigins: [
    "e38c-2804-1494-df7-ad00-1547-5dbb-64eb-7748.ngrok-free.app",
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
