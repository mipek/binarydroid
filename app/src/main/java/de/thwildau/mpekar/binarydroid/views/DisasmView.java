package de.thwildau.mpekar.binarydroid.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.assembly.Disassembler;
import de.thwildau.mpekar.binarydroid.model.Container;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisassemblerViewModel;

public class DisasmView extends ScrollableView {

    private DisassemblerViewModel viewModel;
    private Paint paintAddr;
    private Paint paintText;
    private Container.Section currentSection;
    private float rowHeight;

    public DisasmView(Context context, AttributeSet attrs) {
        super(context, attrs, false, true);

        /*TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HexEditView,*/


        invalidate();
    }

    public DisassemblerViewModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(DisassemblerViewModel viewModel) {
        this.viewModel = viewModel;

        // invalidate paint cache when changing disassembler font size
        /*final DisasmView me = this;
        viewModel.getDisassemblerFontSize().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                me.invalidate();
            }
        });*/
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int desiredWidth = getResources().getDisplayMetrics().widthPixels;
        int desiredHeight = getResources().getDisplayMetrics().heightPixels;

        int width = ViewHelper.handleSize(widthMode, desiredWidth, widthSize);
        int height = ViewHelper.handleSize(heightMode, desiredHeight, heightSize);

        setMeasuredDimension(width, height);
        invalidate();

        Rect r = new Rect();
        paintText.getTextBounds("sample", 0, 6, r);
        rowHeight = r.bottom;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        float density = getResources().getDisplayMetrics().scaledDensity;
        int fontSize = 12;
        /*if (getViewModel() != null && getViewModel().getDisassemblerFontSize() != null) {
            fontSize = getViewModel().getDisassemblerFontSize().getValue();
        }*/

        paintAddr = new Paint();
        paintText = new Paint();

        paintAddr.setColor(Color.GRAY);
        paintAddr.setTextSize(fontSize * density);
        paintAddr.setTextAlign(Paint.Align.LEFT);

        paintText.setColor(Color.BLACK);
        paintText.setTextSize(fontSize * density);
        paintText.setTextAlign(Paint.Align.LEFT);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        if (!isReady()) return;

        Disassembler d = viewModel.getDisasm().getValue();
        long address = viewModel.getAddress().getValue();
        float drawY = getPaddingTop();
        float maxY = getHeight() - getPaddingTop();
        long offset = (int)(getPositionY() / rowHeight) * 4;
        address += offset;
        do {
            Disassembler.Instruction[] insns = d.disassemble(getAccessor(), address, 4); //TODO: don't rely on "bytes" if we want to support x86
            Disassembler.Instruction insn;
            String tmp = String.format("disasm %x got %d insn", address, insns.length);
            Log.d("BinaryDroid", tmp);
            if (insns.length > 1) {
                throw new RuntimeException("unexpected insn count");
            } else if (insns.length == 1) {
                insn = insns[0];
            } else {
                // failed to decode, provide dummy instruction
                insn = Utils.dummyInstruction((short) 4);
            }
            drawRow(address, drawY, insn, canvas);
            address += insn.size();
            drawY += paintAddr.getTextSize() + 1;
        } while(drawY < maxY && address < getTotalBytes());
    }

    private void drawRow(long address, float y, Disassembler.Instruction insn, Canvas c) {
        byte wordSize = getViewModel().getBinary().getValue().getWordSize();
        String szAddr = Utils.l2s(address, wordSize);

        float x = getPaddingLeft();
        c.drawText(szAddr, x, y, paintAddr);
        x += paintAddr.measureText(szAddr) * 2;
        c.drawText(insn.toString(), x, y, paintText);
    }

    /*public void setAddress(long address) {
        currentSection = getSectionOfAddress(address);

    }*/

    private boolean isReady() {
        return viewModel != null && viewModel.getBinary() != null;
    }

    private ByteAccessor getAccessor() {
        return getViewModel().getAccessor().getValue();
    }

    private long getTotalBytes() {
        return getAccessor().getTotalBytes();
    }

    private Container.Section getSectionOfAddress(long va) {
        Container c = viewModel.getBinary().getValue();
        for (Container.Section section: c.getSections()) {
            if (section.va >= va && section.va + section.size < va) {
                return section;
            }
        }
        return null;
    }
}
