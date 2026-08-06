/** @type {import('next').NextConfig} */
const nextConfig = {
  images: {
    unoptimized: true,
  },
  allowedDevOrigins: ["http://localhost:3000"],
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
