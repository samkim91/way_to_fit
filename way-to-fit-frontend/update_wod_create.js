const fs = require('fs');
const path = './src/features/wod/pages/WodCreatePage.tsx';
let content = fs.readFileSync(path, 'utf8');

// Imports
content = content.replace("import { useNavigate, useSearchParams } from 'react-router-dom';", "import { useNavigate } from 'react-router-dom';\nimport { Dialog, DialogContent } from '@/components/ui/dialog';");

// Interface and Component definition
content = content.replace(
  "export default function WodCreatePage() {\n  const navigate = useNavigate();\n  const [searchParams, setSearchParams] = useSearchParams();",
  `interface WodCreatePageProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  selectedDate: string;
}

export default function WodCreatePage({ isOpen, onOpenChange, selectedDate: initialDate }: WodCreatePageProps) {
  const navigate = useNavigate();
  const [selectedDate, setSelectedDate] = useState(initialDate);

  useEffect(() => {
    if (isOpen) {
      setSelectedDate(initialDate);
      setCurrentStep(1);
      setDraftValues(null);
      setPublishMode('auto');
      setHasTouchedClassSelection(false);
      setClassApplyMode('all');
    }
  }, [isOpen, initialDate]);`
);

// Date logic
content = content.replace(
  "  const dateParam = searchParams.get('date');\n  const hasValidDate = !!dateParam && dayjs(dateParam).isValid();\n  const selectedDate = hasValidDate ? dateParam! : '';",
  "  const hasValidDate = !!selectedDate && dayjs(selectedDate).isValid();"
);

// Manual publish date logic
content = content.replace(
  "  const [manualPublishDate, setManualPublishDate] = useState(hasValidDate ? dateParam! : dayjs().format('YYYY-MM-DD'));",
  "  const [manualPublishDate, setManualPublishDate] = useState(initialDate || dayjs().format('YYYY-MM-DD'));"
);

// Remove useEffect for !hasValidDate
content = content.replace(
  /  useEffect\(\(\) => \{\n    if \(!hasValidDate\) \{\n      navigate\('\/wods', \{ replace: true \}\);\n    \}\n  \}, \[hasValidDate, navigate\]\);\n/,
  ""
);

// Form update date handler
content = content.replace(
  "onChange={(e) => setSearchParams({ date: e.target.value })}",
  "onChange={(e) => setSelectedDate(e.target.value)}"
);

// return null logic
content = content.replace(
  "  if (!hasValidDate) {\n    return null;\n  }",
  "  if (!hasValidDate) {\n    return null;\n  }" // leave it or maybe it's fine
);

// Success handler
content = content.replace(
  "      notifySuccess('WOD가 생성되었습니다.');\n      navigate('/wods');",
  "      notifySuccess('WOD가 생성되었습니다.');\n      onOpenChange(false);"
);

// Wrapper
content = content.replace(
  '  return (\n    <div className="container mx-auto max-w-5xl px-4 py-8">',
  '  return (\n    <Dialog open={isOpen} onOpenChange={onOpenChange}>\n      <DialogContent className="max-w-5xl p-0 overflow-hidden bg-transparent border-0 shadow-none">\n        <div className="max-h-[90vh] overflow-y-auto px-4 py-8 bg-background sm:rounded-3xl container mx-auto">'
);

content = content.replace(
  '        <Button variant="ghost" onClick={() => navigate(-1)} className="mb-4 -ml-2 text-muted-foreground">\n          <ChevronLeft className="mr-1 h-4 w-4" />\n          Back\n        </Button>',
  ''
);

// Close tags
content = content.replace(
  '      </div>\n    </div>\n  );\n}',
  '        </div>\n      </DialogContent>\n    </Dialog>\n  );\n}'
);

fs.writeFileSync(path, content);
