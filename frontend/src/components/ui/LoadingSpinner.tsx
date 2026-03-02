/**
 * 로딩 스피너 컴포넌트
 */
export default function LoadingSpinner({ size = "md" }: { size?: "sm" | "md" | "lg" }) {
  const sizeClass = {
    sm: "h-4 w-4 border-2",
    md: "h-8 w-8 border-2",
    lg: "h-12 w-12 border-4",
  }[size];

  return (
    <div className="flex items-center justify-center">
      <div
        className={`${sizeClass} rounded-full border-blue-600 border-t-transparent animate-spin`}
      />
    </div>
  );
}
